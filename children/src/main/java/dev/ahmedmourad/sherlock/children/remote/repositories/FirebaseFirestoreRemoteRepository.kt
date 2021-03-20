package dev.ahmedmourad.sherlock.children.remote.repositories

import arrow.core.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.children.remote.model.QueryId
import dev.ahmedmourad.sherlock.children.remote.model.QueryResult
import dev.ahmedmourad.sherlock.children.remote.utils.*
import dev.ahmedmourad.sherlock.children.repository.dependencies.PreferencesManager
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.FindSimpleUsers
import dev.ahmedmourad.sherlock.domain.data.ObserveSimpleSignedInUser
import dev.ahmedmourad.sherlock.domain.data.ObserveUserAuthState
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
internal class FirebaseFirestoreRemoteRepository @Inject constructor(
        @InternalApi private val db: Lazy<FirebaseFirestore>,
        @InternalApi private val preferencesManager: Lazy<PreferencesManager>,
        private val authStateObservable: ObserveUserAuthState,
        private val simpleSignedInUserObservable: ObserveSimpleSignedInUser,
        private val findSimpleUsers: FindSimpleUsers,
        private val connectivityManager: Lazy<ConnectivityManager>
) : RemoteRepository {

    init {
        if (FirebaseApp.getApps(appCtx).isEmpty()) {
            FirebaseApp.initializeApp(appCtx)
            db.get().firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
        }
    }

    override fun publish(
            childId: ChildId,
            child: ChildToPublish,
            pictureUrl: Url?
    ): Single<Either<RemoteRepository.PublishException, RetrievedChild>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                RemoteRepository.PublishException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.PublishException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.PublishException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected)
                            authStateObservable.invoke().map { either ->
                                either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                            }.firstOrError()
                        else
                            Single.just(
                                    RemoteRepository.PublishException.NoInternetConnectionException.left()
                            )
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createPublish(childId, child, pictureUrl)
                        } else {
                            Single.just(
                                    RemoteRepository.PublishException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createPublish(
            childId: ChildId,
            child: ChildToPublish,
            pictureUrl: Url?
    ): Single<Either<RemoteRepository.PublishException, RetrievedChild>> {

        return Single.create<Either<RemoteRepository.PublishException, RetrievedChild>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(child.toRetrievedChild(
                        childId,
                        System.currentTimeMillis(),
                        pictureUrl
                ).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.PublishException.UnknownException(throwable).left()
                )
            }

            db.get().collection(Contract.Database.Children.PATH)
                    .document(childId.value)
                    .set(child.toMap(pictureUrl))
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun find(
            childId: ChildId
    ): Flowable<Either<RemoteRepository.FindException, RetrievedChild?>> {

        fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
            is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
                RemoteRepository.FindException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.FindException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.FindException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Flowable.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            authStateObservable.invoke().map { either ->
                                either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                            }
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.switchMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Flowable<Either<RemoteRepository.FindException, RetrievedChild?>>>(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createFind(childId)
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createFind(
            childId: ChildId
    ): Flowable<Either<RemoteRepository.FindException, RetrievedChild?>> {

        fun AuthManager.FindSimpleUsersException.map() = when (this) {
            AuthManager.FindSimpleUsersException.NoInternetConnectionException ->
                RemoteRepository.FindException.NoInternetConnectionException
            AuthManager.FindSimpleUsersException.NoSignedInUserException ->
                RemoteRepository.FindException.NoSignedInUserException
            is AuthManager.FindSimpleUsersException.UnknownException ->
                RemoteRepository.FindException.UnknownException(this.origin)
        }

        return Flowable.create<Either<RemoteRepository.FindException, DocumentSnapshot?>>({ emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(
                            RemoteRepository.FindException.UnknownException(exception).left()
                    )

                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onNext(snapshot.right())
                    } else {
                        emitter.onNext(null.right())
                    }
                }
            }

            val registration = db.get().collection(Contract.Database.Children.PATH)
                    .document(childId.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMap { either ->
                    either.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { snapshot ->

                        snapshot ?: return@fold Flowable.just(null.right())

                        val userId = snapshot.getString(Contract.Database.Children.USER_ID)
                                ?.let(::UserId)
                                ?: return@fold Flowable.just(RemoteRepository.FindException.InternalException(
                                        ModelCreationException("Can't find user for child with id: ${childId.value}")
                                ).left())

                        findSimpleUsers.invoke(listOf(userId))
                                .map { either ->
                                    either.fold(ifLeft = {
                                        it.map().left()
                                    }, ifRight = {
                                        it.firstOrNull()?.right()
                                                ?: RemoteRepository.FindException.InternalException(
                                                        ModelCreationException("Can't find user for child with id: ${childId.value}")
                                                ).left()
                                    })
                                }.map { either ->
                                    either.fold(ifLeft = {
                                        it.left()
                                    }, ifRight = { user ->
                                        snapshot.let { s ->
                                            extractRetrievedChild(s, user).mapLeft {
                                                RemoteRepository.FindException.InternalException(it)
                                            }
                                        }
                                    })
                                }
                    })
                }
    }

    //TODO: there needs to be a limit to the number of ongoing investigations
    override fun addInvestigation(
            investigation: Investigation
    ): Single<Either<RemoteRepository.AddInvestigationException, Investigation>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                RemoteRepository.AddInvestigationException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.AddInvestigationException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.AddInvestigationException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected)
                            authStateObservable.invoke().map { either ->
                                either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                            }.firstOrError()
                        else
                            Single.just(
                                    RemoteRepository.AddInvestigationException.NoInternetConnectionException.left()
                            )
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createAddInvestigation(investigation)
                        } else {
                            Single.just(
                                    RemoteRepository.AddInvestigationException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createAddInvestigation(
            investigation: Investigation
    ): Single<Either<RemoteRepository.AddInvestigationException, Investigation>> {
        return Single.create<Either<RemoteRepository.AddInvestigationException, Investigation>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(investigation.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.AddInvestigationException.UnknownException(throwable).left()
                )
            }

            db.get().collection(Contract.Database.Investigations.PATH)
                    .document(UUID.randomUUID().toString())
                    .set(investigation.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    //TODO: this needs to be cached locally
    //TODO: create RetrievedInvestigation and InvestigationToAdd
    override fun findAllInvestigations():
            Flowable<Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>>> {

        fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
            is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
                RemoteRepository.FindAllInvestigationsException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Flowable.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            simpleSignedInUserObservable.invoke().map { option ->
                                option.toEither {
                                    RemoteRepository.FindAllInvestigationsException.NoSignedInUserException
                                }
                            }
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindAllInvestigationsException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.switchMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { signedInUser ->
                        createFindAllInvestigations(signedInUser)
                    })
                }
    }

    private fun createFindAllInvestigations(
            user: SimpleRetrievedUser
    ): Flowable<Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>>> {
        return Flowable.create<Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>>>({ emitter ->

            val snapshotListener = { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(
                            RemoteRepository.FindAllInvestigationsException.UnknownException(exception).left()
                    )

                } else if (snapshot != null) {

                    emitter.onNext(snapshot.documents
                            .filter(DocumentSnapshot::exists)
                            .mapNotNull { extractChildInvestigation(user, it).orNull() }
                            .distinct()
                            .right()
                    )
                }
            }

            val registration = db.get()
                    .collection(Contract.Database.Investigations.PATH)
                    .whereEqualTo(Contract.Database.Investigations.USER_ID, user.id.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    //TODO: this and the firebase functions should add a flag that indicates
    // when the results are ready to harvest, we then filter returned data
    // accordingly to avoid returning incomplete results to the consumer
    override fun findAll(
            query: ChildrenQuery
    ): Flowable<Either<RemoteRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>> {

        fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
            is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
                RemoteRepository.FindAllException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.FindAllException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.FindAllException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Flowable.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            authStateObservable.invoke().map { either ->
                                either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                            }
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindAllException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.switchMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            sendChildQuery(query).toFlowable()
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindAllException.NoSignedInUserException.left()
                            )
                        }
                    })
                }.switchMap { queryIdEither ->
                    queryIdEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = {
                        fetchQueryResults(it)
                    })
                }.switchMap { queryResultsEither ->
                    queryResultsEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { results ->
                        results.chunked(10)
                                .map(this::findChildren)
                                .reduceOrNull { acc, flowable ->
                                    acc.switchMap { firstEither ->
                                        flowable.map { secondEither ->
                                            firstEither.flatMap { first ->
                                                secondEither.map { second -> first + second }
                                            }
                                        }
                                    }
                                } ?: Flowable.just(emptyMap<SimpleRetrievedChild, Weight>().right())
                    })
                }
    }

    private fun sendChildQuery(
            query: ChildrenQuery
    ): Single<Either<RemoteRepository.FindAllException, QueryId>> {
        return Single.create<Either<RemoteRepository.FindAllException, QueryId>> { emitter ->

            val id = preferencesManager.get().getDeviceId()

            val successListener = { _: Void? ->
                emitter.onSuccess(QueryId(id, query.timestamp).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.FindAllException.UnknownException(throwable).left()
                )
            }

            db.get().collection(Contract.Database.Queries.PATH)
                    .document(id)
                    .set(query.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun fetchQueryResults(
            queryId: QueryId
    ): Flowable<Either<RemoteRepository.FindAllException, List<QueryResult>>> {
        return Flowable.create<Either<RemoteRepository.FindAllException, List<QueryResult>>>({ emitter ->

            val snapshotListener = { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(
                            RemoteRepository.FindAllException.UnknownException(exception).left()
                    )

                } else if (snapshot != null) {

                    emitter.onNext(snapshot.documents
                            .filter(DocumentSnapshot::exists)
                            .mapNotNull { extractQueryResult(it).orNull() }
                            .distinct()
                            .right()
                    )
                }
            }

            val registration = db.get()
                    .collection(Contract.Database.Queries.PATH)
                    .document(queryId.value)
                    .collection(Contract.Database.Queries.Results.PATH)
                    .whereEqualTo(Contract.Database.Queries.Results.TIMESTAMP, queryId.timestamp)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .debounce(1, TimeUnit.SECONDS)
    }

    private fun findChildren(
            results: List<QueryResult>
    ): Flowable<Either<RemoteRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>> {

        fun AuthManager.FindSimpleUsersException.map() = when (this) {
            AuthManager.FindSimpleUsersException.NoInternetConnectionException ->
                RemoteRepository.FindAllException.NoInternetConnectionException
            AuthManager.FindSimpleUsersException.NoSignedInUserException ->
                RemoteRepository.FindAllException.NoSignedInUserException
            is AuthManager.FindSimpleUsersException.UnknownException ->
                RemoteRepository.FindAllException.UnknownException(this.origin)
        }

        if (results.isEmpty()) {
            return Flowable.just(emptyMap<SimpleRetrievedChild, Weight>().right())
        }

        return Flowable.create<Either<RemoteRepository.FindAllException, List<DocumentSnapshot>>>({ emitter ->

            val snapshotListener = { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(
                            RemoteRepository.FindAllException.UnknownException(exception).left()
                    )

                } else if (snapshot != null) {

                    emitter.onNext(
                            snapshot.documents.filter(DocumentSnapshot::exists).right()
                    )
                }
            }

            val registration = db.get()
                    .collection(Contract.Database.Children.PATH)
                    .whereIn(FieldPath.documentId(), results.map { it.childId.value })
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .map { either ->
                    either.map { snapshots ->
                        snapshots.mapNotNull { snapshot ->
                            snapshot.getString(Contract.Database.Children.USER_ID)?.let {
                                snapshot to UserId(it)
                            }
                        }
                    }
                }.switchMap { either ->
                    either.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { snapshotsToUserIds ->
                        findSimpleUsers.invoke(snapshotsToUserIds.map(Pair<DocumentSnapshot, UserId>::second).distinct())
                                .map { either ->
                                    either.fold(ifLeft = {
                                        it.map().left()
                                    }) { users ->
                                        snapshotsToUserIds.mapNotNull { (snapshot, id) ->
                                            users.firstOrNull { user ->
                                                user.id == id
                                            }?.let { snapshot to it }
                                        }.mapNotNull { (snapshot, user) ->
                                            extractSimpleRetrievedChild(snapshot, user).orNull()
                                        }.associateWith { child ->
                                            results.first { it.childId == child.id }.weight
                                        }.right()
                                    }
                                }
                    })
                }
    }

    override fun invalidateAllQueries(): Completable {
        return Completable.create { emitter ->

            val id = preferencesManager.get().getDeviceId()

            val successListener = { _: Void? ->
                emitter.onComplete()
            }

            val failureListener = { _: Throwable ->
                emitter.onComplete()
            }

            db.get().collection(Contract.Database.Queries.PATH)
                    .document(id)
                    .delete()
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}
