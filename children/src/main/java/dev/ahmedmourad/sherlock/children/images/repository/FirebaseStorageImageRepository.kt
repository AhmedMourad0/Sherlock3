package dev.ahmedmourad.sherlock.children.images.repository

import android.net.Uri
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.images.contract.Contract
import dev.ahmedmourad.sherlock.children.repository.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.ObserveUserAuthState
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Reusable
internal class FirebaseStorageImageRepository @Inject constructor(
        private val connectivityManager: Lazy<ConnectivityManager>,
        private val authStateObservable: Lazy<ObserveUserAuthState>,
        @InternalApi private val storage: Lazy<FirebaseStorage>
) : ImageRepository {

    override fun storeChildPicture(
            id: ChildId,
            picture: ByteArray?
    ): Single<Either<ImageRepository.StoreChildPictureException, Url?>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                ImageRepository.StoreChildPictureException.UnknownException(this.origin)
        }

        fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
            AuthManager.ObserveUserAuthStateException.NoInternetConnectionException ->
                ImageRepository.StoreChildPictureException.NoInternetConnectionException
            is AuthManager.ObserveUserAuthStateException.UnknownException ->
                ImageRepository.StoreChildPictureException.UnknownException(this.origin)
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
                            authStateObservable.get().invoke().map { either ->
                                either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                            }.firstOrError()
                        } else {
                            Single.just(
                                    ImageRepository.StoreChildPictureException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold<Single<Either<ImageRepository.StoreChildPictureException, StorageReference>>>(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            storePicture(Contract.Children.PATH, id, picture)
                        } else {
                            Single.just(
                                    ImageRepository.StoreChildPictureException.NoSignedInUserException.left()
                            )
                        }
                    })
                }.flatMap { filePath ->
                    filePath.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        fetchPictureUrl(it)
                    })
                }
    }

    private fun storePicture(
            path: String,
            id: ChildId,
            picture: ByteArray
    ): Single<Either<ImageRepository.StoreChildPictureException, StorageReference>> {

        val filePath = storage.get().getReference(path)
                .child("${id.value}.${Contract.FILE_FORMAT}")

        return Single.create<Either<ImageRepository.StoreChildPictureException, StorageReference>> { emitter ->

            val successListener = { _: UploadTask.TaskSnapshot? ->
                emitter.onSuccess(filePath.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        ImageRepository.StoreChildPictureException.UnknownException(throwable).left()
                )
            }

            filePath.putBytes(picture)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun fetchPictureUrl(
            filePath: StorageReference
    ): Single<Either<ImageRepository.StoreChildPictureException, Url>> {

        return Single.create<Either<ImageRepository.StoreChildPictureException, Url>> { emitter ->

            val successListener = { uri: Uri? ->
                emitter.onSuccess(Url.of(uri.toString()).mapLeft {
                    ImageRepository.StoreChildPictureException.InternalException(
                            ModelCreationException(it.toString())
                    )
                })
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        ImageRepository.StoreChildPictureException.UnknownException(throwable).left()
                )
            }

            filePath.downloadUrl
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}
