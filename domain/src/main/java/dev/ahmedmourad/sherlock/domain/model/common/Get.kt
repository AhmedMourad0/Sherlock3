package dev.ahmedmourad.sherlock.domain.model.common

data class Get<I, T>(val id: I, val postTimeStamp: Long, val value: T)
