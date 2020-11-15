package dev.ahmedmourad.sherlock.children.fakes

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import dev.ahmedmourad.sherlock.domain.data.ObserveSimpleSignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import io.reactivex.Flowable

internal class FakeObserveSimpleSignedInUser(
        var userFactory: () -> SimpleRetrievedUser
) : ObserveSimpleSignedInUser {

    var isUserSignedIn = true

    override fun invoke(): Flowable<Option<SimpleRetrievedUser>> {
        return Flowable.defer {
            if (isUserSignedIn) {
                Flowable.just(userFactory().some())
            } else {
                Flowable.just(none())
            }
        }
    }
}
