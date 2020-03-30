package inc.ahmedmourad.sherlock.domain.constants

import inc.ahmedmourad.sherlock.domain.model.children.PublishedChild
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild

sealed class PublishingState {
    data class Success(val child: RetrievedChild) : PublishingState()
    data class Ongoing(val child: PublishedChild) : PublishingState()
    data class Failure(val child: PublishedChild) : PublishingState()
}
