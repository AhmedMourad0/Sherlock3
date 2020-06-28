package dev.ahmedmourad.sherlock.domain.model.auth

import arrow.core.Either
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class CompletedUser private constructor(
        val id: UserId,
        val email: Email,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picture: @Serializable(with = EitherSerializer::class) Either<Url, ByteArray>?
) {
    companion object {
        fun of(id: UserId,
               email: Email,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picture: Either<Url, ByteArray>?
        ): CompletedUser {
            return CompletedUser(id, email, displayName, phoneNumber, picture)
        }
    }
}
