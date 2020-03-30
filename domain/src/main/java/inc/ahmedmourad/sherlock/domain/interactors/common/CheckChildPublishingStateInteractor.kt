package inc.ahmedmourad.sherlock.domain.interactors.common

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.bus.Bus
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import io.reactivex.Single

typealias CheckChildPublishingStateInteractor = () -> @JvmSuppressWildcards Single<Option<PublishingState>>

internal fun checkChildPublishingState(bus: Lazy<Bus>): Single<Option<PublishingState>> {
    return bus.get()
            .childPublishingState
            .map { it.some() }
            .last(none())
}
