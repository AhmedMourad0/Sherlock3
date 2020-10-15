package dev.ahmedmourad.sherlock.domain.constants

sealed class BackgroundState {
    object Ongoing : BackgroundState()
    data class Success(val consume: () -> Unit) : BackgroundState()
    data class Failure(val consume: () -> Unit) : BackgroundState()
    object Idle : BackgroundState()
}
