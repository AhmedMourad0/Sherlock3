package dev.ahmedmourad.sherlock.children.fakes

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.constants.PublishingState

internal class FakeBus : Bus {
    override val childPublishingState: Relay<PublishingState> = BehaviorRelay.create()
    override val childFindingState: Relay<BackgroundState> = BehaviorRelay.create()
    override val childrenFindingState: Relay<BackgroundState> = BehaviorRelay.create()
}
