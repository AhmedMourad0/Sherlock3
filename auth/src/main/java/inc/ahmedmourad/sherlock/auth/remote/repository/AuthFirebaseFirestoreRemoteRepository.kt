package inc.ahmedmourad.sherlock.auth.remote.repository

import androidx.annotation.VisibleForTesting
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Lazy
import inc.ahmedmourad.sherlock.auth.manager.ObserveUserAuthState
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthRemoteRepository
import inc.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import inc.ahmedmourad.sherlock.auth.remote.contract.Contract
import inc.ahmedmourad.sherlock.auth.remote.utils.toMap
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.exceptions.NoInternetConnectionException
import inc.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error

internal class AuthFirebaseFirestoreRemoteRepository(
        private val db: Lazy<FirebaseFirestore>,
        private val connectivityManager: Lazy<ConnectivityManager>,
        private val observeUserAuthState: ObserveUserAuthState
) : AuthRemoteRepository {

    init {
        if (FirebaseApp.getApps(appCtx).isEmpty()) {
            FirebaseApp.initializeApp(appCtx)
            db.get().firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
        }
    }

    override fun storeSignUpUser(user: RemoteSignUpUser): Single<Either<Throwable, SignedInUser>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        observeUserAuthState().map(Boolean::right).singleOrError()
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            store(user)
                        } else {
                            Single.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun store(user: RemoteSignUpUser): Single<Either<Throwable, SignedInUser>> {

        return Single.create<Either<Throwable, SignedInUser>> { emitter ->

            val registrationDate = System.currentTimeMillis()
            val successListener = { _: Void ->
                emitter.onSuccess(user.toSignedInUser(registrationDate).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
            }

            db.get().collection(Contract.Database.Users.PATH)
                    .document(user.id.value)
                    .set(user.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun findSignedInUser(id: UserId): Flowable<Either<Throwable, SignedInUser?>> {
        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        observeUserAuthState().map(Boolean::right)
                    } else {
                        Flowable.just(NoInternetConnectionException().left())
                    }
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createFindUserFlowable(id)
                        } else {
                            Flowable.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun createFindUserFlowable(id: UserId): Flowable<Either<Throwable, SignedInUser?>> {

        return Flowable.create<Either<Throwable, SignedInUser?>>({ emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(exception.left())

                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onNext(extractSignedInUser(snapshot).getOrHandle {
                            Timber.error(it, it::toString)
                            null
                        }.right())
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

    override fun updateUserLastLoginDate(id: UserId): Single<Either<Throwable, Unit>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        observeUserAuthState().map(Boolean::right).singleOrError()
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createUpdateUserLastLoginDateSingle(id, db)
                        } else {
                            Single.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun createUpdateUserLastLoginDateSingle(
            id: UserId,
            db: Lazy<FirebaseFirestore>
    ): Single<Either<Throwable, Unit>> {

        return Single.create<Either<Throwable, Unit>> { emitter ->

            val successListener = { _: Void ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
            }

            db.get().collection(Contract.Database.Users.PATH)
                    .document(id.value)
                    .update(Contract.Database.Users.LAST_LOGIN_DATE, System.currentTimeMillis())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun extractSignedInUser(snapshot: DocumentSnapshot): Either<Throwable, SignedInUser?> {

    val id = UserId(snapshot.id)

    val registrationDate = snapshot.getTimestamp(Contract.Database.Users.REGISTRATION_DATE)
            ?.seconds
            ?.let { it * 1000L }
            ?: return ModelCreationException("publicationDate is null for id=\"$id\"").left()

    return Either.fx {

        val (email) = snapshot.getString(Contract.Database.Users.EMAIL)
                ?.let(Email.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) } ?: return@fx null

        val (displayName) = snapshot.getString(Contract.Database.Users.DISPLAY_NAME)
                ?.let(DisplayName.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) } ?: return@fx null

        val (username) = snapshot.getString(Contract.Database.Users.USER_NAME)
                ?.let(Username.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) } ?: return@fx null

        val countryCode = snapshot.getString(Contract.Database.Users.COUNTRY_CODE) ?: return@fx null

        val number = snapshot.getString(Contract.Database.Users.PHONE_NUMBER) ?: return@fx null

        val (phoneNumber) = PhoneNumber.of(countryCode, number).mapLeft { ModelCreationException(it.toString()) }

        val (pictureUrl) = snapshot.getString(Contract.Database.Users.PICTURE_URL)
                ?.let(Url.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) } ?: null.right()

        SignedInUser.of(
                id,
                registrationDate,
                email,
                displayName,
                username,
                phoneNumber,
                pictureUrl
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}
