package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

typealias ObserveChildPublishingStateInteractor = () -> @JvmSuppressWildcards Flowable<PublishingState>

internal fun observeChildPublishingState(bus: Lazy<Bus>): Flowable<PublishingState> {
    return bus.get().childPublishingState.toFlowable(BackpressureStrategy.LATEST)
}
