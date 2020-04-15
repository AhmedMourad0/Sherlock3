package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.InternalApi
import javax.inject.Inject

interface NotifyChildPublishingStateChangeInteractor : (PublishingState) -> Unit

@Reusable
internal class NotifyChildPublishingStateChangeInteractorImpl @Inject constructor(
        @InternalApi private val bus: Lazy<Bus>
) : NotifyChildPublishingStateChangeInteractor {
    override fun invoke(state: PublishingState) {
        return bus.get().childPublishingState.accept(state)
    }
}
