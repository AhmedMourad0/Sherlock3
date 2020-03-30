package inc.ahmedmourad.sherlock.auth.images.repository

import android.net.Uri
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.Lazy
import inc.ahmedmourad.sherlock.auth.images.contract.Contract
import inc.ahmedmourad.sherlock.auth.manager.ObserveUserAuthState
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthImageRepository
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.exceptions.NoInternetConnectionException
import inc.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal class AuthFirebaseStorageImageRepository(
        private val connectivityManager: Lazy<ConnectivityManager>,
        private val observeUserAuthState: ObserveUserAuthState,
        private val storage: Lazy<FirebaseStorage>
) : AuthImageRepository {

    override fun storeUserPicture(id: UserId, picture: ByteArray?): Single<Either<Throwable, Url?>> {

        if (picture == null) {
            return Single.just(null.right())
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected)
                        observeUserAuthState().map(Boolean::right).firstOrError()
                    else
                        Single.just(NoInternetConnectionException().left())
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            storePicture(Contract.Users.PATH, id, picture)
                        } else {
                            Single.just(NoSignedInUserException().left())
                        }
                    })
                }.flatMap { referenceEither ->
                    referenceEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        fetchPictureUrl(it)
                    })
                }
    }

    private fun storePicture(path: String, id: UserId, picture: ByteArray): Single<Either<Throwable, StorageReference>> {

        val filePath = storage.get().getReference(path)
                .child("${id.value}.${Contract.FILE_FORMAT}")

        return Single.create<Either<Throwable, StorageReference>> { emitter ->

            val successListener = { _: UploadTask.TaskSnapshot ->
                emitter.onSuccess(filePath.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
            }

            filePath.putBytes(picture)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun fetchPictureUrl(filePath: StorageReference): Single<Either<Throwable, Url>> {

        return Single.create<Either<Throwable, Url>> { emitter ->

            val successListener = { uri: Uri ->
                emitter.onSuccess(Url.of(uri.toString()).mapLeft { ModelCreationException(it.toString()) })
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
            }

            filePath.downloadUrl
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}
