package dev.ahmedmourad.sherlock.domain.model.common

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

@Serializable
@NoCopy
data class Url private constructor(val value: String) {

    companion object {

        fun of(value: String): Either<Exception, Url> {
            return validate(value)?.left() ?: Url(value).right()
        }

        fun validate(value: String): Exception? {

            if (value.isBlank()) {
                return Exception.BlankUrlException
            }

            return try {
                URL(value).toURI()
                null
            } catch (e: MalformedURLException) {
                Exception.MalformedUrlException
            } catch (e: URISyntaxException) {
                Exception.MalformedUrlException
            }
        }
    }

    sealed class Exception {
        object MalformedUrlException : Exception()
        object BlankUrlException : Exception()
    }
}
