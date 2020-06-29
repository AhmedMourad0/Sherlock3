package dev.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class PhoneNumber private constructor(val number: String, val countryCode: String) {

    companion object {

        fun of(number: String, countryCode: String = ""): Either<Exception, PhoneNumber> {
            return validate(number, countryCode)?.left()
                    ?: PhoneNumberUtil.getInstance().parse(number, null).let {
                        PhoneNumber(it.nationalNumber.toString(), it.countryCode.toString()).right()
                    }
        }

        fun validate(number: String, countryCode: String = ""): Exception? {
            return when {

                number.isBlank() ->
                    Exception.BlankPhoneNumberException

                number.trim().contains(" ") ->
                    Exception.PhoneNumberContainsWhiteSpacesException(number, countryCode)

                countryCode.trim().contains(" ") ->
                    Exception.CountryCodeContainsWhiteSpacesException(number, countryCode)

                else ->
                    validatePhoneNumber(number, countryCode)
            }
        }

        private fun validatePhoneNumber(number: String, countryCode: String): Exception? {
            return try {
                PhoneNumberUtil.getInstance().parse(number, null)
                null
            } catch (e: NumberParseException) {
                when (e.errorType) {
                    NumberParseException.ErrorType.INVALID_COUNTRY_CODE ->
                        Exception.InvalidCountryCodeException(number, countryCode)
                    NumberParseException.ErrorType.NOT_A_NUMBER ->
                        Exception.InvalidPhoneNumberException(number, countryCode)
                    NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD ->
                        Exception.PhoneNumberTooShortAfterIddException(number, countryCode)
                    NumberParseException.ErrorType.TOO_SHORT_NSN ->
                        Exception.PhoneNumberTooShortException(number, countryCode)
                    NumberParseException.ErrorType.TOO_LONG ->
                        Exception.PhoneNumberTooLongException(number, countryCode)
                    null ->
                        Exception.InvalidPhoneNumberException(number, countryCode)
                }
            }
        }
    }

    sealed class Exception {
        object BlankPhoneNumberException : Exception()
        data class PhoneNumberContainsWhiteSpacesException(val number: String, val countryCode: String) : Exception()
        data class CountryCodeContainsWhiteSpacesException(val number: String, val countryCode: String) : Exception()
        data class PhoneNumberTooShortAfterIddException(val number: String, val countryCode: String) : Exception()
        data class PhoneNumberTooShortException(val number: String, val countryCode: String) : Exception()
        data class PhoneNumberTooLongException(val number: String, val countryCode: String) : Exception()
        data class InvalidCountryCodeException(val number: String, val countryCode: String) : Exception()
        data class InvalidPhoneNumberException(val number: String, val countryCode: String) : Exception()
    }
}
