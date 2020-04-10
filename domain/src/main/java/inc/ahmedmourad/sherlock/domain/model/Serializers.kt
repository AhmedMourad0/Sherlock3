package inc.ahmedmourad.sherlock.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.serialization.*

@Serializer(forClass = Either::class)
class EitherSerializer<A : Any, B : Any>(
        private val leftSerializer: KSerializer<A?>,
        private val rightSerializer: KSerializer<B?>
) : KSerializer<Either<A?, B?>> {

    @OptIn(ImplicitReflectionSerializer::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("arrow.core.Either") {
        element<Boolean>("isRight")
        element("a", leftSerializer.descriptor)
        element("b", rightSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: Either<A?, B?>) {
        encoder.encodeStructure(descriptor) {
            encodeBooleanElement(descriptor, 0, value.isRight())
            when (value) {
                is Either.Left -> {
                    encodeNullableSerializableElement<A>(descriptor, 1, leftSerializer, value.a)
                    encodeNullableSerializableElement<B>(descriptor, 2, rightSerializer, null)
                }
                is Either.Right -> {
                    encodeNullableSerializableElement<A>(descriptor, 1, leftSerializer, null)
                    encodeNullableSerializableElement<B>(descriptor, 2, rightSerializer, value.b)
                }
            }.exhaust()
        }
    }

    override fun deserialize(decoder: Decoder): Either<A?, B?> {
        var isRight: Boolean? = null
        var a: A? = null
        var b: B? = null
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> isRight = decodeBooleanElement(descriptor, i)
                    1 -> a = decodeNullableSerializableElement<A>(descriptor, i, leftSerializer)
                    2 -> b = decodeNullableSerializableElement<B>(descriptor, i, rightSerializer)
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
