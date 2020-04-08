package inc.ahmedmourad.sherlock.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.serialization.*

@Serializer(forClass = Either::class)
class EitherSerializer<A, B>(
        private val leftSerializer: KSerializer<A>,
        private val rightSerializer: KSerializer<B>
) : KSerializer<Either<A, B>> {

    override val descriptor: SerialDescriptor =
            PrimitiveDescriptor("EitherSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Either<A, B>) {
        when (value) {
            is Either.Left -> {
                encoder.encodeBoolean(false)
                leftSerializer.serialize(encoder, value.a)
            }
            is Either.Right -> {
                encoder.encodeBoolean(true)
                rightSerializer.serialize(encoder, value.b)
            }
        }.exhaust()
    }

    override fun deserialize(decoder: Decoder): Either<A, B> {
        return if (decoder.decodeBoolean()) {
            rightSerializer.deserialize(decoder).right()
        } else {
            leftSerializer.deserialize(decoder).left()
        }
    }
}
