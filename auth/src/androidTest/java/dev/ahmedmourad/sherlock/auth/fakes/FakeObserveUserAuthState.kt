package dev.ahmedmourad.sherlock.auth.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.ObserveUserAuthState
import io.reactivex.Flowable

internal class FakeObserveUserAuthState : ObserveUserAuthState {

    var isUserSignedIn = true
    var hasInternet = true
    var triggerUnknownException = false

    override fun invoke(): Flowable<Either<AuthManager.ObserveUserAuthStateException, Boolean>> {
        return Flowable.defer {
            if (triggerUnknownException) {
                Flowable.just(AuthManager.ObserveUserAuthStateException.UnknownException(RuntimeException()).left())
            } else if (!hasInternet) {
                Flowable.just(AuthManager.ObserveUserAuthStateException.NoInternetConnectionException.left())
            } else {
                Flowable.just(isUserSignedIn.right())
            }
        }
    }
}
