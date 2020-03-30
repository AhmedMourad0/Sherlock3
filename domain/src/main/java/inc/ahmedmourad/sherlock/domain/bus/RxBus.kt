package inc.ahmedmourad.sherlock.domain.bus

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import inc.ahmedmourad.sherlock.domain.constants.BackgroundState
import inc.ahmedmourad.sherlock.domain.constants.PublishingState

internal class RxBus : Bus {
    override val childPublishingState: Relay<PublishingState> = BehaviorRelay.create()
    override val childFindingState: Relay<BackgroundState> = BehaviorRelay.create()
    override val childrenFindingState: Relay<BackgroundState> = BehaviorRelay.create()
}
