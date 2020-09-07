package dev.ahmedmourad.sherlock.domain.utils

import kotlinx.datetime.*
import java.security.MessageDigest

fun <T> T.exhaust() = this

fun localDateTime(millis: Long): LocalDateTime {
    return Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
}

fun LocalDateTime.toMillis(): Long {
    return this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun hash(str: String): ByteArray {
    return MessageDigest.getInstance("MD5").digest(str.toByteArray())
}
