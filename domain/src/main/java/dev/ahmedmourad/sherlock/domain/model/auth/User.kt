package dev.ahmedmourad.sherlock.domain.model.auth

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class User private constructor(
        val id: UserId,
        val timestamp: Long,
        val lastLoginTimestamp: Long,
        val email: Email,
        val displayName: DisplayName,
        val username: Username,
        val phoneNumber: PhoneNumber,
        val pictureUrl: Url?
) {

    companion object {
        fun of(id: UserId,
               timestamp: Long,
               lastLoginTimestamp: Long,
               email: Email,
               displayName: DisplayName,
               username: Username,
               phoneNumber: PhoneNumber,
               pictureUrl: Url?
        ): User {
            return User(
                    id,
                    timestamp,
                    lastLoginTimestamp,
                    email,
                    displayName,
                    username,
                    phoneNumber,
                    pictureUrl
            )
        }
    }
}
