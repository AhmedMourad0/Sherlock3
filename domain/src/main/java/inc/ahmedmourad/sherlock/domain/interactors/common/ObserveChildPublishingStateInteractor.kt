package inc.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.bus.Bus
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

typealias ObserveChildPublishingStateInteractor = () -> @JvmSuppressWildcards Flowable<PublishingState>

internal fun observeChildPublishingState(bus: Lazy<Bus>): Flowable<PublishingState> {
    return bus.get().childPublishingState.toFlowable(BackpressureStrategy.LATEST)
}
