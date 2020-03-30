package inc.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable
import io.reactivex.Single

internal interface AuthAuthenticator {

    fun observeUserAuthState(): Flowable<Boolean>

    fun getCurrentUser(): Flowable<Either<Throwable, IncompleteUser>>

    fun signIn(credentials: UserCredentials): Single<Either<Throwable, IncompleteUser>>

    fun signUp(credentials: UserCredentials): Single<Either<Throwable, IncompleteUser>>

    fun signInWithGoogle(): Single<Either<Throwable, IncompleteUser>>

    fun signInWithFacebook(): Single<Either<Throwable, IncompleteUser>>

    fun signInWithTwitter(): Single<Either<Throwable, IncompleteUser>>

    fun sendPasswordResetEmail(email: Email): Single<Either<Throwable, Unit>>

    fun signOut(): Single<Either<Throwable, UserId?>>
}
