package inc.ahmedmourad.sherlock.auth.model

import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import timber.log.Timber
import timber.log.error

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
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }
}
