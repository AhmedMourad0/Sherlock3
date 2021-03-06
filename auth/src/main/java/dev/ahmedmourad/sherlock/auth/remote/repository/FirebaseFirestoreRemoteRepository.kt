package dev.ahmedmourad.sherlock.auth.remote.repository

import androidx.annotation.VisibleForTesting
import arrow.core.*
import arrow.core.extensions.fx
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.di.InternalApi
import dev.ahmedmourad.sherlock.auth.manager.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.auth.remote.contract.Contract
import dev.ahmedmourad.sherlock.auth.remote.utils.toMap
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.ObserveUserAuthState
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import javax.inject.Inject

@Reusable
internal class FirebaseFirestoreRemoteRepository @Inject constructor(
        @InternalApi private val db: Lazy<FirebaseFirestore>,
        private val connectivityManager: Lazy<ConnectivityManager>,
        private val authStateObservable: ObserveUserAuthState
) : RemoteRepository {

    init {
        if (FirebaseApp.getApps(appCtx).isEmpty()) {
            FirebaseApp.initializeApp(appCtx)
            db.get().firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
        }
    }

    override fun storeSignUpUser(
            user: RemoteSignUpUser
    ): Single<Either<RemoteRepository.StoreSignUpUserException, SignedInUser>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                RemoteRepository.StoreSignUpUserException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.StoreSignUpUserException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.StoreSignUpUserException.UnknownException(this.origin)
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
                            authStateObservable.invoke()
                                    .map { it.mapLeft(AuthManager.ObserveUserAuthStateException::map) }
                                    .firstOrError()
                        } else {
                            Single.just(
                                    RemoteRepository.StoreSignUpUserException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Single<Either<RemoteRepository.StoreSignUpUserException, SignedInUser>>>(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createStoreSignUpUser(user)
                        } else {
                            Single.just(
                                    RemoteRepository.StoreSignUpUserException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createStoreSignUpUser(
            user: RemoteSignUpUser
    ): Single<Either<RemoteRepository.StoreSignUpUserException, SignedInUser>> {

        return Single.create<Either<RemoteRepository.StoreSignUpUserException, SignedInUser>> { emitter ->

            val timestamp = System.currentTimeMillis()
            val successListener = { _: Void? ->
                emitter.onSuccess(user.toSignedInUser(timestamp).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.StoreSignUpUserException.UnknownException(throwable).left()
                )
            }

            db.get().collection(Contract.Database.Users.PATH)
                    .document(user.id.value)
                    .set(user.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun findSignedInUser(
            id: UserId
    ): Flowable<Either<RemoteRepository.FindSignedInUserException, SignedInUser?>> {

        fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
            is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
                RemoteRepository.FindSignedInUserException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.FindSignedInUserException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.FindSignedInUserException.UnknownException(this.origin)
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
                            authStateObservable.invoke()
                                    .map { it.mapLeft(AuthManager.ObserveUserAuthStateException::map) }
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindSignedInUserException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.switchMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Flowable<Either<RemoteRepository.FindSignedInUserException, SignedInUser?>>>(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createFindSignedInUser(id)
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindSignedInUserException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createFindSignedInUser(
            id: UserId
    ): Flowable<Either<RemoteRepository.FindSignedInUserException, SignedInUser?>> {

        return Flowable.create<Either<RemoteRepository.FindSignedInUserException, SignedInUser?>>({ emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {
                    emitter.onNext(
                            RemoteRepository.FindSignedInUserException.UnknownException(exception).left()
                    )
                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onNext(extractSignedInUser(snapshot).mapLeft {
                            RemoteRepository.FindSignedInUserException.InternalException(
                                    ModelCreationException(it.toString())
                            )
                        })
                    } else {
                        emitter.onNext(null.right())
                    }
                }
            }

            val registration = db.get().collection(Contract.Database.Users.PATH)
                    .document(id.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun findSimpleUsers(
            ids: Collection<UserId>
    ): Flowable<Either<RemoteRepository.FindSimpleUsersException, List<SimpleRetrievedUser>>> {

        fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
            is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
                RemoteRepository.FindSimpleUsersException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.FindSimpleUsersException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.FindSimpleUsersException.UnknownException(this.origin)
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
                            authStateObservable.invoke()
                                    .map { it.mapLeft(AuthManager.ObserveUserAuthStateException::map) }
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindSimpleUsersException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.switchMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Flowable<Either<RemoteRepository.FindSimpleUsersException, List<SimpleRetrievedUser>>>>(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            ids.chunked(10)
                                    .map(this::createFindSimpleUsers)
                                    .reduceOrNull { acc, flowable ->
                                        acc.switchMap { firstEither ->
                                            flowable.map { secondEither ->
                                                firstEither.flatMap { first ->
                                                    secondEither.map { second -> first + second }
                                                }
                                            }
                                        }
                                    } ?: Flowable.just(emptyList<SimpleRetrievedUser>().right())
                        } else {
                            Flowable.just(
                                    RemoteRepository.FindSimpleUsersException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createFindSimpleUsers(
            ids: Collection<UserId>
    ): Flowable<Either<RemoteRepository.FindSimpleUsersException, List<SimpleRetrievedUser>>> {

        return Flowable.create<Either<RemoteRepository.FindSimpleUsersException, List<SimpleRetrievedUser>>>({ emitter ->

            val snapshotListener = { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {
                    emitter.onNext(
                            RemoteRepository.FindSimpleUsersException.UnknownException(exception).left()
                    )
                } else if (snapshot != null) {
                    emitter.onNext(snapshot.documents
                            .filter(DocumentSnapshot::exists)
                            .mapNotNull {
                                extractSimpleUser(it).orNull()
                            }.right()
                    )
                }
            }

            val registration = db.get().collection(Contract.Database.Users.PATH)
                    .whereIn(FieldPath.documentId(), ids.map(UserId::value))
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun updateUserLastLoginDate(
            id: UserId
    ): Single<Either<RemoteRepository.UpdateUserLastLoginDateException, Unit>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                RemoteRepository.UpdateUserLastLoginDateException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                RemoteRepository.UpdateUserLastLoginDateException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                RemoteRepository.UpdateUserLastLoginDateException.UnknownException(this.origin)
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
                            authStateObservable.invoke()
                                    .map { it.mapLeft(AuthManager.ObserveUserAuthStateException::map) }
                                    .firstOrError()
                        } else {
                            Single.just(
                                    RemoteRepository.UpdateUserLastLoginDateException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Single<Either<RemoteRepository.UpdateUserLastLoginDateException, Unit>>>(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createUpdateUserLastLoginDate(id)
                        } else {
                            Single.just(
                                    RemoteRepository.UpdateUserLastLoginDateException.NoSignedInUserException.left()
                            )
                        }
                    })
                }
    }

    private fun createUpdateUserLastLoginDate(
            id: UserId
    ): Single<Either<RemoteRepository.UpdateUserLastLoginDateException, Unit>> {

        return Single.create<Either<RemoteRepository.UpdateUserLastLoginDateException, Unit>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                if (throwable is FirebaseFirestoreException && throwable.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    emitter.onSuccess(Unit.right())
                } else {
                    emitter.onSuccess(
                            RemoteRepository.UpdateUserLastLoginDateException.UnknownException(throwable).left()
                    )
                }
            }

            db.get().collection(Contract.Database.Users.PATH)
                    .document(id.value)
                    .update(Contract.Database.Users.LAST_LOGIN_TIMESTAMP, System.currentTimeMillis())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun extractSignedInUser(snapshot: DocumentSnapshot): Either<Throwable, SignedInUser?> {

    val id = UserId(snapshot.id)

    return Either.fx {

        val timestamp = snapshot.getTimestamp(Contract.Database.Users.TIMESTAMP)
                ?.toDate()
                ?.time ?: return@fx null

        val email = snapshot.getString(Contract.Database.Users.EMAIL)
                ?.let(Email.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }?.bind() ?: return@fx null

        val displayName = snapshot.getString(Contract.Database.Users.DISPLAY_NAME)
                ?.let(DisplayName.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }?.bind() ?: return@fx null

        val username = snapshot.getString(Contract.Database.Users.USER_NAME)
                ?.let(Username.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }?.bind() ?: return@fx null

        val countryCode = snapshot.getString(Contract.Database.Users.COUNTRY_CODE) ?: return@fx null

        val number = snapshot.getString(Contract.Database.Users.PHONE_NUMBER) ?: return@fx null

        val phoneNumber = !PhoneNumber.of(number, countryCode)
                .mapLeft { ModelCreationException(it.toString()) }

        val pictureUrl = snapshot.getString(Contract.Database.Users.PICTURE_URL)
                ?.let(Url.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }?.bind()

        SignedInUser.of(
                id,
                timestamp,
                email,
                displayName,
                username,
                phoneNumber,
                pictureUrl
        ).right().bind()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun extractSimpleUser(snapshot: DocumentSnapshot): Either<Throwable, SimpleRetrievedUser?> {

    val id = UserId(snapshot.id)

    return Either.fx {

        val displayName = snapshot.getString(Contract.Database.Users.DISPLAY_NAME)
                ?.let(DisplayName.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }?.bind() ?: return@fx null

        val pictureUrl = snapshot.getString(Contract.Database.Users.PICTURE_URL)
                ?.let(Url.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }?.bind()

        SimpleRetrievedUser.of(
                id,
                displayName,
                pictureUrl
        ).right().bind()
    }
}
