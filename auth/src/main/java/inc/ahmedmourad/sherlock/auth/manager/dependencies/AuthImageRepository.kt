package inc.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single

internal interface AuthImageRepository {
    fun storeUserPicture(id: UserId, picture: ByteArray?): Single<Either<Throwable, Url?>>
}
