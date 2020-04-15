package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import javax.inject.Inject

typealias NotifyChildrenFindingStateChangeInteractor =
        (@JvmSuppressWildcards BackgroundState) -> @JvmSuppressWildcards Unit

@Reusable
internal class NotifyChildrenFindingStateChangeInteractorImpl @Inject constructor(
        private val bus: Lazy<Bus>
) : NotifyChildrenFindingStateChangeInteractor {
    override fun invoke(state: BackgroundState) {
        return bus.get().childrenFindingState.accept(state)
    }
}
