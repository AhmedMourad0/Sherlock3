package dev.ahmedmourad.sherlock.android.model.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId

@NoCopy
internal data class AppCompletedUser private constructor(
        val id: UserId,
        val email: Email,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picture: Either<Url, PicturePath>?
) {

    fun toCompletedUser(imageLoader: ImageLoader): CompletedUser {
        return CompletedUser.of(
                id,
                email,
                displayName,
                phoneNumber,
                picture?.fold(ifLeft = Url::left, ifRight = {
                    imageLoader.getBytesOrNull(it.value)?.right()
                })
        )
    }

    companion object {
        fun of(id: UserId,
               email: Email,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picturePath: Either<Url, PicturePath>?
        ): AppCompletedUser {
            return AppCompletedUser(id, email, displayName, phoneNumber, picturePath)
        }
    }
}
