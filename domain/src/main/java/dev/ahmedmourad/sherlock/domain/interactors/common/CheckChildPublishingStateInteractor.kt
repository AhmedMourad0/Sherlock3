package dev.ahmedmourad.sherlock.domain.interactors.common

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import io.reactivex.Single
import javax.inject.Inject

typealias CheckChildPublishingStateInteractor =
        () -> @JvmSuppressWildcards Single<Option<PublishingState>>

@Reusable
internal class CheckChildPublishingStateInteractorImpl @Inject constructor(
        private val bus: Lazy<Bus>
) : CheckChildPublishingStateInteractor {
    override fun invoke(): Single<Option<PublishingState>> {
        return bus.get()
                .childPublishingState
                .map { it.some() }
                .last(none())
    }
}
