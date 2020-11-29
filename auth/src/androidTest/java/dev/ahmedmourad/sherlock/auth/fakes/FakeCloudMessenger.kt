package dev.ahmedmourad.sherlock.auth.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.auth.authenticator.messaging.CloudMessenger
import io.reactivex.Single

internal class FakeCloudMessenger : CloudMessenger {

    var fail = false

    val subscriptions: MutableList<String> = mutableListOf()

    override fun subscribe(topic: String): Single<Either<Throwable, String>> {
        return Single.fromCallable {
            if (fail) {
                RuntimeException().left()
            } else {
                subscriptions.add(topic)
                topic.right()
            }
        }
    }

    override fun unsubscribe(topic: String): Single<Either<Throwable, String>> {
        return Single.fromCallable {
            if (fail) {
                RuntimeException().left()
            } else {
                subscriptions.removeIf { it == topic }
                topic.right()
            }
        }
    }
}
