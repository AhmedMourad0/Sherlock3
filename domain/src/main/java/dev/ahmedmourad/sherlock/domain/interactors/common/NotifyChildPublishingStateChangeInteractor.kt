package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState

typealias NotifyChildPublishingStateChangeInteractor = (@JvmSuppressWildcards PublishingState) -> @JvmSuppressWildcards Unit

internal fun notifyChildPublishingStateChange(bus: Lazy<Bus>, state: PublishingState) {
    return bus.get().childPublishingState.accept(state)
}
