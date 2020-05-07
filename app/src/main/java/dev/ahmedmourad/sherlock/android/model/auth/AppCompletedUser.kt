package dev.ahmedmourad.sherlock.android.model.auth

import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import dev.ahmedmourad.sherlock.domain.model.ids.UserId

internal class AppCompletedUser private constructor(
        val id: UserId,
        val email: Email,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picturePath: PicturePath?
) {

    fun toCompletedUser(imageLoader: ImageLoader): CompletedUser {
        return CompletedUser.of(
                id,
                email,
                displayName,
                phoneNumber,
                imageLoader.getBytesOrNull(picturePath?.value)
        )
    }

    fun component1() = id

    fun component2() = email

    fun component3() = displayName

    fun component4() = phoneNumber

    fun component5() = picturePath

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as AppCompletedUser

        if (id != other.id)
            return false

        if (email != other.email)
            return false

        if (displayName != other.displayName)
            return false

        if (phoneNumber != other.phoneNumber)
            return false

        if (picturePath != other.picturePath)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + picturePath.hashCode()
        return result
    }

    override fun toString(): String {
        return "AppCompletedUser(" +
                "id=$id, " +
                "email=$email, " +
                "displayName=$displayName, " +
                "phoneNumber=$phoneNumber, " +
                "picturePath=$picturePath" +
                ")"
    }

    companion object {
        fun of(id: UserId,
               email: Email,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picturePath: PicturePath?
        ): AppCompletedUser {
            return AppCompletedUser(id, email, displayName, phoneNumber, picturePath)
        }
    }
}
