package inc.ahmedmourad.sherlock.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.serialization.*

@Serializer(forClass = Either::class)
class EitherSerializer<A, B>(
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
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeBooleanElement(descriptor, 0, value.isRight())
        when (value) {
            is Either.Left -> {
                compositeOutput.encodeNullableSerializableElement(descriptor, 1, leftSerializer, value.a)
                compositeOutput.encodeNullableSerializableElement(descriptor, 2, rightSerializer, null)
            }
            is Either.Right -> {
                compositeOutput.encodeNullableSerializableElement(descriptor, 1, leftSerializer, null)
                compositeOutput.encodeNullableSerializableElement(descriptor, 2, rightSerializer, value.b)
            }
        }.exhaust()
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Either<A?, B?> {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
        var isRight: Boolean? = null
        var a: A? = null
        var b: B? = null
        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.READ_DONE -> break@loop
                0 -> isRight = dec.decodeBooleanElement(descriptor, i)
                1 -> a = dec.decodeNullableSerializableElement(descriptor, i, leftSerializer)
                2 -> b = dec.decodeNullableSerializableElement(descriptor, i, rightSerializer)
                else -> throw SerializationException("Unknown index: $i")
            }
        }
        dec.endStructure(descriptor)
        isRight ?: throw SerializationException("Cannot find field: isRight")
        return if (isRight) {
            b.right()
        } else {
            a.left()
        }
    }
}
