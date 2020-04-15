package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState

typealias NotifyChildFindingStateChangeInteractor =
        (@JvmSuppressWildcards BackgroundState) -> @JvmSuppressWildcards Unit

internal class NotifyChildFindingStateChangeInteractorImpl(
        private val bus: Lazy<Bus>
) : NotifyChildFindingStateChangeInteractor {
    override fun invoke(state: BackgroundState) {
        return bus.get().childFindingState.accept(state)
    }
}
