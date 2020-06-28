package dev.ahmedmourad.sherlock.android.model.validators.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.interpreters.model.localizedMessage
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.auth.AppCompletedUser
import dev.ahmedmourad.sherlock.android.model.auth.AppSignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import splitties.init.appCtx

internal fun validateEmail(value: String?): Either<String, Email> {

    if (value == null) {
        return appCtx.getString(R.string.email_empty_or_blank).left()
    }

    return Email.of(value).mapLeft(Email.Exception::localizedMessage)
}

internal fun validatePassword(value: String?): Either<String, Password> {

    if (value == null) {
        return appCtx.getString(R.string.password_empty_or_blank).left()
    }

    return Password.of(value).mapLeft(Password.Exception::localizedMessage)
}

internal fun validatePasswordConfirmation(value: String?, password: Password): Either<String, Unit> {

    if (value == null) {
        return appCtx.getString(R.string.password_confirmation_empty_or_blank).left()
    }

    if (value != password.value) {
        return appCtx.getString(R.string.password_confirmation_does_not_match).left()
    }

    return Unit.right()
}

internal fun validateDisplayName(value: String?): Either<String, DisplayName> {

    if (value == null) {
        return appCtx.getString(R.string.display_name_empty_or_blank).left()
    }

    return DisplayName.of(value).mapLeft(DisplayName.Exception::localizedMessage)
}

internal fun validatePhoneNumber(countryCode: String?, number: String?): Either<String, PhoneNumber> {

    if (number == null) {
        return appCtx.getString(R.string.phone_number_empty_or_blank).left()
    }

    if (countryCode == null) {
        return appCtx.getString(R.string.invalid_country_code).left()
    }

    return PhoneNumber.of(number, countryCode).mapLeft(PhoneNumber.Exception::localizedMessage)
}

internal fun validateAppCompletedUser(
        id: UserId,
        email: Email,
        displayName: DisplayName,
        phoneNumber: PhoneNumber,
        picturePath: Either<Url, PicturePath>?
): Either<String, AppCompletedUser> {
    return AppCompletedUser.of(
            id,
            email,
            displayName,
            phoneNumber,
            picturePath
    ).right()
}

internal fun validateUserCredentials(
        email: Email,
        password: Password
): Either<String, UserCredentials> {
    return UserCredentials.of(
            email,
            password
    ).mapLeft(UserCredentials.Exception::localizedMessage)
}

internal fun validateAppSignUpUser(
        credentials: UserCredentials,
        displayName: DisplayName,
        phoneNumber: PhoneNumber,
        picturePath: PicturePath?,
        imageLoader: ImageLoader
): Either<String, AppSignUpUser> {
    return AppSignUpUser.of(
            credentials,
            displayName,
            phoneNumber,
            picturePath,
            imageLoader
    ).mapLeft(SignUpUser.Exception::localizedMessage)
}
