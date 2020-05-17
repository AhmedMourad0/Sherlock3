package dev.ahmedmourad.sherlock.auth.images.repository

import android.net.Uri
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.di.InternalApi
import dev.ahmedmourad.sherlock.auth.images.contract.Contract
import dev.ahmedmourad.sherlock.auth.manager.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.UserAuthStateObservable
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Reusable
internal class FirebaseStorageImageRepository @Inject constructor(
        private val connectivityManager: Lazy<ConnectivityManager>,
        @InternalApi private val userAuthStateObservable: UserAuthStateObservable,
        @InternalApi private val storage: Lazy<FirebaseStorage>
) : ImageRepository {

    override fun storeUserPicture(
            id: UserId,
            picture: ByteArray?
    ): Single<Either<ImageRepository.StoreUserPictureException, Url?>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                ImageRepository.StoreUserPictureException.UnknownException(this.origin)
        }

        if (picture == null) {
            return Single.just(null.right())
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
                            userAuthStateObservable.observeUserAuthState()
                                    .map(Boolean::right)
                                    .firstOrError()
                        } else {
                            Single.just(
                                    ImageRepository.StoreUserPictureException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Single<Either<ImageRepository.StoreUserPictureException, StorageReference>>>(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createStoreUserPicture(Contract.Users.PATH, id, picture)
                        } else {
                            Single.just(
                                    ImageRepository.StoreUserPictureException.NoSignedInUserException.left()
                            )
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

    private fun createStoreUserPicture(
            path: String,
            id: UserId,
            picture: ByteArray
    ): Single<Either<ImageRepository.StoreUserPictureException, StorageReference>> {

        val filePath = storage.get().getReference(path)
                .child("${id.value}.${Contract.FILE_FORMAT}")

        return Single.create<Either<ImageRepository.StoreUserPictureException, StorageReference>> { emitter ->

            val successListener = { _: UploadTask.TaskSnapshot ->
                emitter.onSuccess(filePath.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        ImageRepository.StoreUserPictureException.UnknownException(throwable).left()
                )
            }

            filePath.putBytes(picture)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun fetchPictureUrl(
            filePath: StorageReference
    ): Single<Either<ImageRepository.StoreUserPictureException, Url>> {

        return Single.create<Either<ImageRepository.StoreUserPictureException, Url>> { emitter ->

            val successListener = { uri: Uri ->
                emitter.onSuccess(
                        Url.of(uri.toString()).mapLeft {
                            ImageRepository.StoreUserPictureException.InternalException(
                                    ModelCreationException(it.toString())
                            )
                        }
                )
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        ImageRepository.StoreUserPictureException.UnknownException(throwable).left()
                )
            }

            filePath.downloadUrl
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}
