package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import javax.inject.Inject

typealias NotifyChildPublishingStateChangeInteractor =
        (@JvmSuppressWildcards PublishingState) -> @JvmSuppressWildcards Unit

@Reusable
internal class NotifyChildPublishingStateChangeInteractorImpl @Inject constructor(
        private val bus: Lazy<Bus>
) : NotifyChildPublishingStateChangeInteractor {
    override fun invoke(state: PublishingState) {
        return bus.get().childPublishingState.accept(state)
    }
}
