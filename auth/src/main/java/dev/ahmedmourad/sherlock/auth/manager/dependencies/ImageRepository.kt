package dev.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single

internal interface ImageRepository {
    fun storeUserPicture(id: UserId, picture: ByteArray?): Single<Either<Throwable, Url?>>
}
