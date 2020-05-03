package dev.ahmedmourad.sherlock.domain.model.auth

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class IncompleteUser private constructor(
        val id: UserId,
        val email: Email?,
        val displayName: DisplayName?,
        val phoneNumber: PhoneNumber?,
        val pictureUrl: Url?
) {

    companion object {
        fun of(id: UserId,
               email: Email?,
               displayName: DisplayName?,
               phoneNumber: PhoneNumber?,
               pictureUrl: Url?
        ): IncompleteUser {
            return IncompleteUser(id, email, displayName, phoneNumber, pictureUrl)
        }
    }
}
