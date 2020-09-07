package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Flowable
import javax.inject.Inject

fun interface ObserveUserAuthStateInteractor :
        () -> Flowable<Either<ObserveUserAuthStateInteractor.Exception, Boolean>> {
    sealed class Exception {
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.ObserveUserAuthStateException.map() = when (this) {
    is AuthManager.ObserveUserAuthStateException.UnknownException ->
        ObserveUserAuthStateInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class ObserveUserAuthStateInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : ObserveUserAuthStateInteractor {
    override fun invoke(): Flowable<Either<ObserveUserAuthStateInteractor.Exception, Boolean>> {
        return authManager.get()
                .observeUserAuthState()
                .map { either ->
                    either.mapLeft(AuthManager.ObserveUserAuthStateException::map)
                }
    }
}
