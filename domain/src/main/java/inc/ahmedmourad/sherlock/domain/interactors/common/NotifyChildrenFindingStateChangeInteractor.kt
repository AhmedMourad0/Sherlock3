package inc.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.bus.Bus
import inc.ahmedmourad.sherlock.domain.constants.BackgroundState

typealias NotifyChildrenFindingStateChangeInteractor = (@JvmSuppressWildcards BackgroundState) -> @JvmSuppressWildcards Unit

internal fun notifyChildrenFindingStateChange(bus: Lazy<Bus>, state: BackgroundState) {
    return bus.get().childrenFindingState.accept(state)
}
