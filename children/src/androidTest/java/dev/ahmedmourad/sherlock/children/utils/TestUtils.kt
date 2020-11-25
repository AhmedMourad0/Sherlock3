package dev.ahmedmourad.sherlock.children.utils

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
