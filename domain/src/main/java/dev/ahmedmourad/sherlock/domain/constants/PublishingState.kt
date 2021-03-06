package dev.ahmedmourad.sherlock.domain.constants

import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild

sealed class PublishingState {
    data class Success(
            val child: RetrievedChild,
            val consume: () -> Unit
    ) : PublishingState()

    data class Ongoing(val child: ChildToPublish) : PublishingState()
    data class Failure constructor(
            val child: ChildToPublish,
            val error: Exception,
            val consume: () -> Unit
    ) : PublishingState()

    object Idle : PublishingState()

    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}
