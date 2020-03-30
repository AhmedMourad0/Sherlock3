package inc.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import inc.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable
import io.reactivex.Single

internal interface AuthRemoteRepository {

    fun storeSignUpUser(user: RemoteSignUpUser): Single<Either<Throwable, SignedInUser>>

    fun findSignedInUser(id: UserId): Flowable<Either<Throwable, SignedInUser?>>

    fun updateUserLastLoginDate(id: UserId): Single<Either<Throwable, Unit>>
}
