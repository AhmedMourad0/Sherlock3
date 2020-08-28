package dev.ahmedmourad.sherlock.domain.model.auth

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class SimpleRetrievedUser private constructor(
        val id: UserId,
        val displayName: DisplayName,
        val pictureUrl: Url?
) {

    companion object {
        fun of(id: UserId,
               displayName: DisplayName,
               pictureUrl: Url?
        ): SimpleRetrievedUser {
            return SimpleRetrievedUser(
                    id,
                    displayName,
                    pictureUrl
            )
        }
    }
}
