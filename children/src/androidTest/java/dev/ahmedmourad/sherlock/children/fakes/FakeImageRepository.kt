package dev.ahmedmourad.sherlock.children.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.children.repository.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Single

internal class FakeImageRepository : ImageRepository {

    var hasInternet = true
    var isUserSignedIn = true
    var triggerInternalException = false
    var triggerUnknownException = false

    override fun storeChildPicture(
            id: ChildId,
            picture: ByteArray?
    ): Single<Either<ImageRepository.StoreChildPictureException, Url?>> {
        return Single.defer {
            when {

                !hasInternet -> {
                    Single.just(ImageRepository.StoreChildPictureException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Single.just(ImageRepository.StoreChildPictureException.NoSignedInUserException.left())
                }

                triggerInternalException -> {
                    Single.just(ImageRepository.StoreChildPictureException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Single.just(ImageRepository.StoreChildPictureException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Single.just(null.right())
                }
            }
        }
    }
}
