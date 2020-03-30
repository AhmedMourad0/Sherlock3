package inc.ahmedmourad.sherlock.auth.authenticator.bus

import arrow.core.Either
import com.google.firebase.auth.AuthCredential
import com.jakewharton.rxrelay2.PublishRelay

internal object AuthenticatorBus {
    val signInCompletion = PublishRelay.create<Either<Throwable, AuthCredential>>()
    val signInCancellation = PublishRelay.create<Unit>()
}
