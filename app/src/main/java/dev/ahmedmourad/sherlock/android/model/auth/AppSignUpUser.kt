package dev.ahmedmourad.sherlock.android.model.auth

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import timber.log.Timber
import timber.log.error

@NoCopy
internal data class AppSignUpUser private constructor(
        val credentials: UserCredentials,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picturePath: PicturePath?
) {

    fun toSignUpUser(imageLoader: ImageLoader): SignUpUser {
        return SignUpUser.of(
                credentials,
                displayName,
                phoneNumber,
                imageLoader.getBytesOrNull(picturePath?.value)
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    companion object {
        fun of(credentials: UserCredentials,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picturePath: PicturePath?,
               imageLoader: ImageLoader
        ): Either<SignUpUser.Exception, AppSignUpUser> {
            return SignUpUser.validate(
                    credentials,
                    displayName,
                    phoneNumber,
                    imageLoader.getBytesOrNull(picturePath?.value)
            )?.left() ?: AppSignUpUser(credentials, displayName, phoneNumber, picturePath).right()
        }
    }
}
