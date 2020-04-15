package dev.ahmedmourad.sherlock.domain.bus

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BusImpl @Inject constructor() : Bus {
    override val childPublishingState: Relay<PublishingState> = BehaviorRelay.create()
    override val childFindingState: Relay<BackgroundState> = BehaviorRelay.create()
    override val childrenFindingState: Relay<BackgroundState> = BehaviorRelay.create()
}
