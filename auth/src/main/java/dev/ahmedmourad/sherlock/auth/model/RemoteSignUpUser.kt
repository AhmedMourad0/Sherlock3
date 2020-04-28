package dev.ahmedmourad.sherlock.auth.model

import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId

internal data class RemoteSignUpUser(
        val id: UserId,
        val email: Email,
        val username: Username,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val pictureUrl: Url?
) {
    fun toSignedInUser(registrationDate: Long): SignedInUser {
        return SignedInUser.of(
                id,
                registrationDate,
                email,
                displayName,
                username,
                phoneNumber,
                pictureUrl
        )
    }
}
