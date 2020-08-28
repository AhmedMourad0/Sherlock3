package dev.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Single

internal interface ImageRepository {

    fun storeChildPicture(
            id: ChildId,
            picture: ByteArray?
    ): Single<Either<StoreChildPictureException, Url?>>

    sealed class StoreChildPictureException {
        object NoInternetConnectionException : StoreChildPictureException()
        object NoSignedInUserException : StoreChildPictureException()
        data class InternalException(val origin: Throwable) : StoreChildPictureException()
        data class UnknownException(val origin: Throwable) : StoreChildPictureException()
    }
}
