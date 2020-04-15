package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState

typealias NotifyChildPublishingStateChangeInteractor =
        (@JvmSuppressWildcards PublishingState) -> @JvmSuppressWildcards Unit

internal class NotifyChildPublishingStateChangeInteractorImpl(
        private val bus: Lazy<Bus>
) : NotifyChildPublishingStateChangeInteractor {
    override fun invoke(state: PublishingState) {
        return bus.get().childPublishingState.accept(state)
    }
}
