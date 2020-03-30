package inc.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.bus.Bus
import inc.ahmedmourad.sherlock.domain.constants.PublishingState

typealias NotifyChildPublishingStateChangeInteractor = (@JvmSuppressWildcards PublishingState) -> @JvmSuppressWildcards Unit

internal fun notifyChildPublishingStateChange(bus: Lazy<Bus>, state: PublishingState) {
    return bus.get().childPublishingState.accept(state)
}
