package dev.ahmedmourad.sherlock.auth.authenticator.messaging

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.di.InternalApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Reusable
internal class FirebaseCloudMessenger @Inject constructor(
        @InternalApi private val messaging: Lazy<FirebaseMessaging>
) : CloudMessenger {

    override fun subscribe(topic: String): Single<Either<Throwable, String>> {
        return Single.create<Either<Throwable, String>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(topic.right())
            }

            val failureListener = { e: Exception ->
                emitter.onSuccess(e.left())
            }

            messaging.get().subscribeToTopic(topic)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun unsubscribe(topic: String): Single<Either<Throwable, String>> {
        return Single.create<Either<Throwable, String>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(topic.right())
            }

            val failureListener = { e: Exception ->
                emitter.onSuccess(e.left())
            }

            messaging.get().unsubscribeFromTopic(topic)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}
