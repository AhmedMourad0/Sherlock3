package dev.ahmedmourad.sherlock.domain.model.common

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable
import java.io.File
import java.util.*

@Serializable
@NoCopy
data class PicturePath private constructor(val value: String) {

    companion object {

        fun of(value: String): Either<Exception, PicturePath> {
            return validate(value)?.left() ?: PicturePath(value).right()
        }

        fun validate(value: String): Exception? {

            if (value.isBlank()) {
                return Exception.BlankPathException
            }

            try {

                val file = File(value)

                return if (!file.exists()) {
                    Exception.NonExistentFileException
                } else if (!file.isFile) {
                    Exception.NonFilePathException
                } else if (!file.canRead()) {
                    Exception.UnreadableFileException
                } else if (file.extension.toLowerCase(Locale.US) == "gif") {
                    Exception.GifPathException
                } else if (file.extension.toLowerCase(Locale.US) !in arrayOf("jpg", "jpeg", "png")) {
                    Exception.NonPicturePathException
                } else {
                    null
                }

            } catch (e: SecurityException) {
                return Exception.SecurityException
            }
        }
    }

    sealed class Exception {
        object BlankPathException : Exception()
        object NonExistentFileException : Exception()
        object NonFilePathException : Exception()
        object NonPicturePathException : Exception()
        object GifPathException : Exception()
        object UnreadableFileException : Exception()
        object SecurityException : Exception()
    }
}
