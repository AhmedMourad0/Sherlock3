package dev.ahmedmourad.sherlock.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

@Serializer(forClass = Either::class)
class EitherSerializer<A : Any, B : Any>(
        private val leftSerializer: KSerializer<A?>,
        private val rightSerializer: KSerializer<B?>
) : KSerializer<Either<A?, B?>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("arrow.core.Either") {
        element<Boolean>("isRight")
        element("a", leftSerializer.descriptor)
        element("b", rightSerializer.descriptor)
    }

    @ExperimentalSerializationApi
    override fun serialize(encoder: Encoder, value: Either<A?, B?>) {
        encoder.encodeStructure(descriptor) {
            encodeBooleanElement(descriptor, 0, value.isRight())
            when (value) {
                is Either.Left -> {
                    encodeNullableSerializableElement(descriptor, 1, leftSerializer, value.a)
                    encodeNullableSerializableElement(descriptor, 2, rightSerializer, null)
                }
                is Either.Right -> {
                    encodeNullableSerializableElement(descriptor, 1, leftSerializer, null)
                    encodeNullableSerializableElement(descriptor, 2, rightSerializer, value.b)
                }
            }.exhaust()
        }
    }

    @ExperimentalSerializationApi
    override fun deserialize(decoder: Decoder): Either<A?, B?> {
        var isRight: Boolean? = null
        var a: A? = null
        var b: B? = null
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> isRight = decodeBooleanElement(descriptor, i)
                    1 -> a = decodeNullableSerializableElement(descriptor, i, leftSerializer)
                    2 -> b = decodeNullableSerializableElement(descriptor, i, rightSerializer)
                    else -> throw SerializationException("Unknown index: $i")
                }
            }
        }
        return if (isRight ?: throw SerializationException("Cannot find field: isRight")) {
            b.right()
        } else {
            a.left()
        }
    }
}
