package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.InternalApi
import javax.inject.Inject

interface NotifyChildFindingStateChangeInteractor : (BackgroundState) -> Unit

@Reusable
internal class NotifyChildFindingStateChangeInteractorImpl @Inject constructor(
        @InternalApi private val bus: Lazy<Bus>
) : NotifyChildFindingStateChangeInteractor {
    override fun invoke(state: BackgroundState) {
        return bus.get().childFindingState.accept(state)
    }
}
