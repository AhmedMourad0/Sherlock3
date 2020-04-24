package dev.ahmedmourad.sherlock.domain.dagger

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class InternalApi
