package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState

typealias NotifyChildFindingStateChangeInteractor = (@JvmSuppressWildcards BackgroundState) -> @JvmSuppressWildcards Unit

internal fun notifyChildFindingStateChange(bus: Lazy<Bus>, state: BackgroundState) {
    return bus.get().childFindingState.accept(state)
}
