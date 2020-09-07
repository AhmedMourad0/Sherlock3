package dev.ahmedmourad.sherlock.android.interpreters.model

import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.*
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import splitties.init.appCtx

internal fun Name.Exception.localizedMessage(): String {
    return when (this) {
        Name.Exception.BlankNameException ->
            appCtx.getString(R.string.name_empty_or_blank)
        Name.Exception.NameContainsWhiteSpacesException ->
            appCtx.getString(R.string.name_contains_white_spaces)
        is Name.Exception.NameTooShortException ->
            appCtx.getString(R.string.name_too_short, this.minLength)
        is Name.Exception.NameTooLongException ->
            appCtx.getString(R.string.name_too_long, this.maxLength)
        Name.Exception.NameContainsNumbersOrSymbolsException ->
            appCtx.getString(R.string.name_contains_numbers_or_symbols)
    }
}

internal fun Age.Exception.localizedMessage(): String {
    return when (this) {
        is Age.Exception.AgeOutOfRangeException ->
            appCtx.getString(R.string.age_out_of_range, this.min, this.max)
    }
}

internal fun AgeRange.Exception.localizedMessage(): String {
    return when (this) {
        is AgeRange.Exception.MinExceedsMaxException ->
            appCtx.getString(R.string.age_min_exceeds_max)
    }
}

internal fun Height.Exception.localizedMessage(): String {
    return when (this) {
        is Height.Exception.HeightOutOfRangeException ->
            appCtx.getString(R.string.height_out_of_range, this.min, this.max)
    }
}

internal fun HeightRange.Exception.localizedMessage(): String {
    return when (this) {
        is HeightRange.Exception.MinExceedsMaxException ->
            appCtx.getString(R.string.height_min_exceeds_max)
    }
}

internal fun Coordinates.Exception.localizedMessage(): String {
    return when (this) {
        Coordinates.Exception.InvalidLatitudeException ->
            appCtx.getString(R.string.invalid_latitude)
        Coordinates.Exception.InvalidLongitudeException ->
            appCtx.getString(R.string.invalid_longitude)
        Coordinates.Exception.InvalidCoordinatesException ->
            appCtx.getString(R.string.invalid_coordinates)
    }
}

internal fun ApproximateAppearance.Exception.localizedMessage(): String {
    return when (this) {
        ApproximateAppearance.Exception.NotEnoughDetailsException ->
            appCtx.getString(R.string.few_appearance_details)
    }
}

internal fun ChildToPublish.Exception.localizedMessage(): String {
    return when (this) {
        ChildToPublish.Exception.NotEnoughDetailsException ->
            appCtx.getString(R.string.child_not_enough_details)
    }
}

internal fun ChildrenQuery.Exception.localizedMessage(): String {
    return when (this) {
        ChildrenQuery.Exception.InvalidPageNumberException ->
            appCtx.getString(R.string.something_went_wrong)
    }
}

internal fun Email.Exception.localizedMessage(): String {
    return when (this) {
        Email.Exception.BlankEmailException ->
            appCtx.getString(R.string.email_empty_or_blank)
        is Email.Exception.EmailContainsWhiteSpacesException ->
            appCtx.getString(R.string.email_contains_whitespaces)
        is Email.Exception.MalformedEmailException ->
            appCtx.getString(R.string.malformed_email)
    }
}

internal fun Password.Exception.localizedMessage(): String {
    return when (this) {
        Password.Exception.BlankPasswordException ->
            appCtx.getString(R.string.password_empty_or_blank)
        is Password.Exception.PasswordTooShortException ->
            appCtx.getString(R.string.password_too_short, this.minLength)
        Password.Exception.NoCapitalLettersException ->
            appCtx.getString(R.string.password_no_capital_letters)
        Password.Exception.NoSmallLettersException ->
            appCtx.getString(R.string.password_no_small_letters)
        Password.Exception.NoDigitsException ->
            appCtx.getString(R.string.password_no_digits)
        Password.Exception.NoSymbolsException ->
            appCtx.getString(R.string.password_no_symbols)
        is Password.Exception.FewDistinctCharactersException ->
            appCtx.getString(R.string.password_few_distinct_characters, this.min)
        Password.Exception.OnlySequentialCharactersOrDigitsException ->
            appCtx.getString(R.string.password_contains_only_sequential_chars_or_digits)
    }
}

internal fun DisplayName.Exception.localizedMessage(): String {
    return when (this) {
        DisplayName.Exception.BlankDisplayNameException ->
            appCtx.getString(R.string.display_name_empty_or_blank)
        is DisplayName.Exception.DisplayNameTooShortException ->
            appCtx.getString(R.string.display_name_too_short, this.minLength)
        is DisplayName.Exception.DisplayNameTooLongException ->
            appCtx.getString(R.string.display_name_too_long, this.maxLength)
        DisplayName.Exception.DisplayNameContainsNumbersOrSymbolsException ->
            appCtx.getString(R.string.display_name_contains_numbers_or_symbols)
        is DisplayName.Exception.SingleNameException ->
            this.exception.localizedMessage()
    }
}

internal fun PhoneNumber.Exception.localizedMessage(): String {
    return when (this) {
        PhoneNumber.Exception.BlankPhoneNumberException ->
            appCtx.getString(R.string.invalid_phone_number)
        is PhoneNumber.Exception.PhoneNumberContainsWhiteSpacesException ->
            appCtx.getString(R.string.phone_number_contains_whitespaces)
        is PhoneNumber.Exception.CountryCodeContainsWhiteSpacesException ->
            appCtx.getString(R.string.country_code_contains_whitespaces)
        is PhoneNumber.Exception.PhoneNumberTooShortAfterIddException ->
            appCtx.getString(R.string.international_phone_number_too_short)
        is PhoneNumber.Exception.PhoneNumberTooShortException ->
            appCtx.getString(R.string.phone_number_too_short)
        is PhoneNumber.Exception.PhoneNumberTooLongException ->
            appCtx.getString(R.string.phone_number_too_long)
        is PhoneNumber.Exception.InvalidCountryCodeException ->
            appCtx.getString(R.string.invalid_country_code)
        is PhoneNumber.Exception.InvalidPhoneNumberException ->
            appCtx.getString(R.string.invalid_phone_number)
    }
}

internal fun UserCredentials.Exception.localizedMessage(): String {
    return when (this) {
        UserCredentials.Exception.EmailIsUsedAsPasswordException ->
            appCtx.getString(R.string.email_used_as_password)
    }
}

internal fun SignUpUser.Exception.localizedMessage(): String {
    return when (this) {
        SignUpUser.Exception.DisplayNameIsUsedAsPasswordException ->
            appCtx.getString(R.string.display_name_used_as_password)
        SignUpUser.Exception.PhoneNumberIsUsedAsPasswordException ->
            appCtx.getString(R.string.phone_number_used_as_password)
    }
}
