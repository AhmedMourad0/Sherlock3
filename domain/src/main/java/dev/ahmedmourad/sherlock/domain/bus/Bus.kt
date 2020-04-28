package dev.ahmedmourad.sherlock.domain.bus

import com.jakewharton.rxrelay2.Relay
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.constants.PublishingState

interface Bus {
    val childPublishingState: Relay<PublishingState>
    val childFindingState: Relay<BackgroundState>
    val childrenFindingState: Relay<BackgroundState>
}
