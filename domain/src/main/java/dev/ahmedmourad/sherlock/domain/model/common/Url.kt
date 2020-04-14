package dev.ahmedmourad.sherlock.domain.model.common

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

@Serializable
class Url private constructor(val value: String) {

    fun component1() = value

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Url

        if (value != other.value)
            return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Url(value=$value)"
    }

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
