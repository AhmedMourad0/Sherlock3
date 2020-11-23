package dev.ahmedmourad.sherlock.auth.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.auth.manager.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single

internal class FakeImageRepository : ImageRepository {

    var hasInternet = true
    var isUserSignedIn = true
    var triggerInternalException = false
    var triggerUnknownException = false

    override fun storeUserPicture(
            id: UserId,
            picture: ByteArray?
    ): Single<Either<ImageRepository.StoreUserPictureException, Url?>> {
        return Single.defer {
            when {

                !hasInternet -> {
                    Single.just(ImageRepository.StoreUserPictureException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Single.just(ImageRepository.StoreUserPictureException.NoSignedInUserException.left())
                }

                triggerInternalException -> {
                    Single.just(ImageRepository.StoreUserPictureException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Single.just(ImageRepository.StoreUserPictureException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Single.just(null.right())
                }
            }
        }
    }
}
