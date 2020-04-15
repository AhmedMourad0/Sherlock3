package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.InternalApi
import javax.inject.Inject

interface NotifyChildrenFindingStateChangeInteractor : (BackgroundState) -> Unit

@Reusable
internal class NotifyChildrenFindingStateChangeInteractorImpl @Inject constructor(
        @InternalApi private val bus: Lazy<Bus>
) : NotifyChildrenFindingStateChangeInteractor {
    override fun invoke(state: BackgroundState) {
        return bus.get().childrenFindingState.accept(state)
    }
}
