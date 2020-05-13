package dev.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single

internal interface ImageRepository {
    fun storeUserPicture(id: UserId, picture: ByteArray?): Single<Either<StoreUserPictureException, Url?>>

    sealed class StoreUserPictureException {
        object NoInternetConnectionException : StoreUserPictureException()
        object NoSignedInUserException : StoreUserPictureException()
        data class InternalException(val origin: Throwable) : StoreUserPictureException()
        data class UnknownException(val origin: Throwable) : StoreUserPictureException()
    }
}
