package dev.ahmedmourad.sherlock.auth.authenticator.bus

import arrow.core.Either
import com.google.firebase.auth.AuthCredential
import com.jakewharton.rxrelay2.PublishRelay
import dev.ahmedmourad.sherlock.auth.authenticator.AuthActivityFactory

internal object AuthenticatorBus {
    val signInCompletion = PublishRelay.create<Either<AuthActivityFactory.Exception, AuthCredential>>()
    val signInCancellation = PublishRelay.create<Unit>()
}
