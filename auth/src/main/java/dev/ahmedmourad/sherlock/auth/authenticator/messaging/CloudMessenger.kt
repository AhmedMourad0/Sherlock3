package dev.ahmedmourad.sherlock.auth.authenticator.messaging

import arrow.core.Either
import io.reactivex.Single

internal interface CloudMessenger {
    fun subscribe(topic: String): Single<Either<Throwable, String>>
    fun unsubscribe(topic: String): Single<Either<Throwable, String>>
}