package dev.ahmedmourad.sherlock.children.remote.repositories

import androidx.annotation.VisibleForTesting
import arrow.core.*
import arrow.core.extensions.fx
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.children.remote.utils.toMap
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.FindSimpleUsers
import dev.ahmedmourad.sherlock.domain.data.ObserveUserAuthState
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

@Reusable
internal class FirebaseFirestoreRemoteRepository @Inject constructor(
        @InternalApi private val db: Lazy<FirebaseFirestore>,
        private val authStateObservable: ObserveUserAuthState,
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
                    isUserSignedInEither.fold<Single<Either<RemoteRepository.PublishException, RetrievedChild>>>(ifLeft = {
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
            is AuthManager.FindSimpleUsersException.InternalException ->
                RemoteRepository.FindException.InternalException(this.origin)
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
                .flatMap { either ->
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
                                    }) { user ->
                                        snapshot.let { s ->
                                            extractRetrievedChild(s, user).mapLeft {
                                                RemoteRepository.FindException.InternalException(it)
                                            }
                                        }
                                    }
                                }
                    })
                }
    }

    override fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<RemoteRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>> {

        fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
            is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
                RemoteRepository.FindAllException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
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
                    isUserSignedInEither.fold<Flowable<Either<RemoteRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>>>(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createFindAll(filter)
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindAllException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createFindAll(
            filter: Filter<RetrievedChild>
    ): Flowable<Either<RemoteRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>> {

        fun AuthManager.FindSimpleUsersException.map() = when (this) {
            AuthManager.FindSimpleUsersException.NoInternetConnectionException ->
                RemoteRepository.FindAllException.NoInternetConnectionException
            AuthManager.FindSimpleUsersException.NoSignedInUserException ->
                RemoteRepository.FindAllException.NoSignedInUserException
            is AuthManager.FindSimpleUsersException.InternalException ->
                RemoteRepository.FindAllException.InternalException(this.origin)
            is AuthManager.FindSimpleUsersException.UnknownException ->
                RemoteRepository.FindAllException.UnknownException(this.origin)
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

            //This's all going to change
            val registration = db.get()
                    .collection(Contract.Database.Children.PATH)
                    .limit(20)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { either ->
                    either.map { snapshots ->
                        snapshots.mapNotNull { snapshot ->
                            snapshot.getString(Contract.Database.Children.USER_ID)?.let {
                                snapshot to UserId(it)
                            }
                        }
                    }
                }.flatMap { either ->
                    either.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { snapshotsToUserIds ->
                        findSimpleUsers.invoke(snapshotsToUserIds.map(Pair<DocumentSnapshot, UserId>::second))
                                .map { either ->
                                    either.fold(ifLeft = {
                                        it.map().left()
                                    }) { users ->
                                        filter.filter(users.mapNotNull { user ->
                                            snapshotsToUserIds.firstOrNull { (_, id) ->
                                                id == user.id
                                            }?.let { pair -> pair.first to user }
                                        }.mapNotNull { (snapshot, user) ->
                                            extractRetrievedChild(snapshot, user).orNull()
                                        }).mapKeys { it.key.simplify() }.right()
                                    }
                                }
                    })
                }
    }

    override fun clear(): Single<Either<RemoteRepository.ClearException, Unit>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                RemoteRepository.ClearException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.ClearException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            authStateObservable.invoke().map { either ->
                                either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                            }.firstOrError()
                        } else {
                            Single.just(
                                    RemoteRepository.ClearException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Single<Either<RemoteRepository.ClearException, Unit>>>(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        if (it) {
                            createClear()
                        } else {
                            Single.just(
                                    RemoteRepository.ClearException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createClear(): Single<Either<RemoteRepository.ClearException, Unit>> {

        return Single.create<Either<RemoteRepository.ClearException, Unit>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.ClearException.UnknownException(throwable).left()
                )
            }

            val querySuccessListener: (QuerySnapshot) -> Unit = { snapshot: QuerySnapshot ->
                Tasks.whenAll(snapshot.documents.map { it.reference.delete() })
                        .addOnSuccessListener(successListener)
                        .addOnFailureListener(failureListener)
            }

            db.get().collection(Contract.Database.Children.PATH)
                    .get()
                    .addOnSuccessListener(querySuccessListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun extractRetrievedChild(
        snapshot: DocumentSnapshot,
        user: SimpleRetrievedUser
): Either<ModelCreationException, RetrievedChild> {

    val id = snapshot.id

    val timestamp = snapshot.getTimestamp(Contract.Database.Children.TIMESTAMP)
            ?.seconds
            ?.let { it * 1000L }
            ?: return ModelCreationException("timestamp is null for id=$id").left()

    val pictureUrl = snapshot.getString(Contract.Database.Children.PICTURE_URL)
            ?.let(Url.Companion::of)
            ?.mapLeft { ModelCreationException(it.toString()) }
            ?.getOrHandle {
                Timber.error(it, it::toString)
                null
            }

    val name = extractName(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    val location = extractLocation(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    return Either.fx {

        val appearance = !extractApproximateAppearance(snapshot)

        RetrievedChild.of(
                ChildId(id),
                user,
                timestamp,
                name,
                snapshot.getString(Contract.Database.Children.NOTES),
                location,
                appearance,
                pictureUrl
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractName(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, Either<Name, FullName>?> {
    return Either.fx {

        val first = snapshot.getString(Contract.Database.Children.FIRST_NAME) ?: return@fx null

        val firstName = !Name.of(first).mapLeft { ModelCreationException(it.toString()) }

        val last = snapshot.getString(Contract.Database.Children.LAST_NAME)
                ?: return@fx firstName.left()

        val lastName = !Name.of(last).mapLeft { ModelCreationException(it.toString()) }

        FullName.of(firstName, lastName).right().right().bind()
    }
}

private fun extractApproximateAppearance(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, ApproximateAppearance> {
    return Either.fx {

        val gender = snapshot.getLong(Contract.Database.Children.GENDER)?.toInt()
                ?.let { findEnum(it, Gender.values()) }

        val skin = snapshot.getLong(Contract.Database.Children.SKIN)?.toInt()
                ?.let { findEnum(it, Skin.values()) }

        val hair = snapshot.getLong(Contract.Database.Children.HAIR)?.toInt()
                ?.let { findEnum(it, Hair.values()) }

        val ageRange = !extractAgeRange(snapshot).mapLeft { ModelCreationException(it.toString()) }

        val heightRange = !extractHeightRange(snapshot).mapLeft { ModelCreationException(it.toString()) }

        ApproximateAppearance.of(
                gender,
                skin,
                hair,
                ageRange,
                heightRange
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractAgeRange(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, AgeRange?> {
    return Either.fx {

        val min = snapshot.getLong(Contract.Database.Children.MIN_AGE)?.toInt() ?: return@fx null

        val minAge = !Age.of(min).mapLeft { ModelCreationException(it.toString()) }

        val max = snapshot.getLong(Contract.Database.Children.MAX_AGE)?.toInt() ?: return@fx null

        val maxAge = !Age.of(max).mapLeft { ModelCreationException(it.toString()) }

        AgeRange.of(minAge, maxAge).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractHeightRange(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, HeightRange?> {
    return Either.fx {

        val min = snapshot.getLong(Contract.Database.Children.MIN_HEIGHT)?.toInt() ?: return@fx null

        val minHeight = !Height.of(min).mapLeft { ModelCreationException(it.toString()) }

        val max = snapshot.getLong(Contract.Database.Children.MAX_HEIGHT)?.toInt() ?: return@fx null

        val maxHeight = !Height.of(max).mapLeft { ModelCreationException(it.toString()) }

        HeightRange.of(minHeight, maxHeight).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractLocation(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, Location?> {
    return Either.fx {

        val locationId = snapshot.getString(Contract.Database.Children.LOCATION_ID)
                ?: return@fx null
        val locationName = snapshot.getString(Contract.Database.Children.LOCATION_NAME)
                ?: return@fx null
        val locationAddress = snapshot.getString(Contract.Database.Children.LOCATION_ADDRESS)
                ?: return@fx null

        val coordinates = extractCoordinates(snapshot).bind() ?: return@fx null

        Location.of(
                locationId,
                locationName,
                locationAddress,
                coordinates
        ).right().bind()
    }
}

private fun extractCoordinates(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, Coordinates?> {

    val latitude = snapshot.getDouble(Contract.Database.Children.LOCATION_LATITUDE)
            ?: return null.right()
    val longitude = snapshot.getDouble(Contract.Database.Children.LOCATION_LONGITUDE)
            ?: return null.right()

    return Coordinates.of(latitude, longitude).mapLeft { ModelCreationException(it.toString()) }
}
