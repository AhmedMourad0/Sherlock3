package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import dev.ahmedmourad.sherlock.domain.dagger.InternalApi
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject

interface ObserveChildPublishingStateInteractor : () -> Flowable<PublishingState>

@Reusable
internal class ObserveChildPublishingStateInteractorImpl @Inject constructor(
        @InternalApi private val bus: Lazy<Bus>
) : ObserveChildPublishingStateInteractor {
    override fun invoke(): Flowable<PublishingState> {
        return bus.get().childPublishingState.toFlowable(BackpressureStrategy.LATEST)
    }
}
