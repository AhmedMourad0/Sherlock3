package inc.ahmedmourad.sherlock.domain.bus

import com.jakewharton.rxrelay2.Relay
import inc.ahmedmourad.sherlock.domain.constants.BackgroundState
import inc.ahmedmourad.sherlock.domain.constants.PublishingState

internal interface Bus {
    val childPublishingState: Relay<PublishingState>
    val childFindingState: Relay<BackgroundState>
    val childrenFindingState: Relay<BackgroundState>
}
