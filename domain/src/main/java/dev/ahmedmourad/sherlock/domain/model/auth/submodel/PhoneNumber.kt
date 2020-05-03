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
                    ?: PhoneNumberUtil.getInstance().parse(number, countryCode).let {
                        PhoneNumber(it.nationalNumber.toString(), it.countryCode.toString()).right()
                    }
        }

        fun validate(number: String, countryCode: String = ""): Exception? {
            return when {

                number.isBlank() -> Exception.BlankPhoneNumberException

                number.trim().contains(" ") -> Exception.PhoneNumberContainsWhiteSpacesException

                countryCode.trim().contains(" ") -> Exception.CountryCodeContainsWhiteSpacesException

                else -> validatePhoneNumber(number, countryCode)
            }
        }

        private fun validatePhoneNumber(number: String, countryCode: String): Exception? {
            return try {
                val phoneUtil = PhoneNumberUtil.getInstance()
                val numberProto = phoneUtil.parse(number, countryCode)
                if (numberProto.hasCountryCode() && phoneUtil.isValidNumber(numberProto)) {
                    null
                } else {
                    Exception.InvalidPhoneNumberException
                }
            } catch (e: NumberParseException) {
                when (e.errorType) {
                    NumberParseException.ErrorType.INVALID_COUNTRY_CODE -> Exception.InvalidCountryCodeException
                    NumberParseException.ErrorType.NOT_A_NUMBER -> Exception.InvalidPhoneNumberException
                    NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD -> Exception.PhoneNumberTooShortAfterIddException
                    NumberParseException.ErrorType.TOO_SHORT_NSN -> Exception.PhoneNumberTooShortException
                    NumberParseException.ErrorType.TOO_LONG -> Exception.PhoneNumberTooLongException
                    null -> Exception.InvalidPhoneNumberException
                }
            }
        }
    }

    sealed class Exception {
        object BlankPhoneNumberException : Exception()
        object PhoneNumberContainsWhiteSpacesException : Exception()
        object CountryCodeContainsWhiteSpacesException : Exception()
        object PhoneNumberTooShortAfterIddException : Exception()
        object PhoneNumberTooShortException : Exception()
        object PhoneNumberTooLongException : Exception()
        object InvalidCountryCodeException : Exception()
        object InvalidPhoneNumberException : Exception()
    }
}
