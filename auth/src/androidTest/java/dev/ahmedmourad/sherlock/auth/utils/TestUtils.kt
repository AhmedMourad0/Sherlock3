package dev.ahmedmourad.sherlock.auth.utils

import arrow.core.orNull
import arrow.core.right
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import java.util.*

internal fun randomString(
        minLength: Int,
        maxLength: Int = minLength,
        allowUpperCaseLetters: Boolean = true,
        allowLowerCaseLetters: Boolean = true,
        allowDigits: Boolean = true,
        allowSymbols: Boolean = false
): String {

    val allowed: List<Char> = mutableListOf<Char>().run {
        if (allowUpperCaseLetters) plus('A'..'Z') else this
    }.run {
        if (allowLowerCaseLetters) plus('a'..'z') else this
    }.run {
        if (allowDigits) plus('0'..'9') else this
    }.run {
        if (allowSymbols) plus("!@#\\$%&*()_+=|<>?{}[]~-.".toCharArray().toList()) else this
    }

    val length = (minLength..maxLength).random()

    if (allowed.isEmpty() || length < 1) {
        return ""
    }

    return (1..length).map { allowed.random() }.joinToString("")
}

internal fun signUpUser(): SignUpUser {
    return SignUpUser.of(
            userCredentials(),
            randomDisplayName(),
            PhoneNumber.of("+201200608893", null).orNull()!!,
            null
    ).orNull()!!
}

internal fun completedUser(
        id: UserId = UserId(UUID.randomUUID().toString()),
        email: Email = randomEmail()
): CompletedUser {
    return CompletedUser.of(
            id,
            email,
            randomDisplayName(),
            PhoneNumber.of("+201200608893", null).orNull()!!,
            ByteArray(0).right()
    )
}

internal fun userCredentials(): UserCredentials {
    return UserCredentials.of(randomEmail(), randomPassword()).orNull()!!
}

internal fun incompleteUser(id: UserId = UserId(UUID.randomUUID().toString())): IncompleteUser {
    return IncompleteUser.of(
            id,
            randomEmail(),
            randomDisplayName(),
            PhoneNumber.of("+201200608893", null).orNull()!!,
            null
    )
}

internal fun remoteSignUpUser(id: UserId, email: Email): RemoteSignUpUser {
    val displayName = randomDisplayName()
    return RemoteSignUpUser(
            id,
            email,
            Username.from(displayName),
            displayName,
            PhoneNumber.of("+201200608893", null).orNull()!!,
            null
    )
}

internal fun randomDisplayName(): DisplayName {
    return DisplayName.of(
            (1..(2..3).random()).joinToString(" ") { randomName().value }
    ).orNull()!!
}

internal fun randomName(): Name {
    return Name.of(
            randomString(
                    4,
                    9,
                    allowUpperCaseLetters = true,
                    allowLowerCaseLetters = true,
                    allowDigits = false,
                    allowSymbols = false
            )
    ).orNull()!!
}

internal fun randomEmail(): Email {
    return Email.of(
            randomString(7, 12, allowUpperCaseLetters = false) +
                    "@${randomString(4, 7, allowUpperCaseLetters = false, allowDigits = false)}" +
                    ".${randomString(2, 4, allowUpperCaseLetters = false, allowDigits = false)}"
    ).orNull()!!
}

internal fun randomPassword(): Password {

    val upperCase = randomString(
            3,
            5,
            allowUpperCaseLetters = true,
            allowLowerCaseLetters = false,
            allowDigits = false,
            allowSymbols = false
    )

    val lowerCase = randomString(
            3,
            5,
            allowUpperCaseLetters = false,
            allowLowerCaseLetters = true,
            allowDigits = false,
            allowSymbols = false
    )

    val digits = randomString(
            3,
            5,
            allowUpperCaseLetters = false,
            allowLowerCaseLetters = false,
            allowDigits = true,
            allowSymbols = false
    )

    val symbols = randomString(
            3,
            5,
            allowUpperCaseLetters = false,
            allowLowerCaseLetters = false,
            allowDigits = false,
            allowSymbols = true
    )

    return Password.of(
            (upperCase + lowerCase + digits + symbols).toCharArray()
                    .apply { shuffle() }
                    .joinToString("")
    ).orNull()!!
}

data class FirebaseToken(val value: String)
