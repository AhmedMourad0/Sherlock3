package inc.ahmedmourad.sherlock.model.parcelers.utils

import android.os.Parcel
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.android.parcel.Parceler

internal fun <T> Parceler<T>.writeNullable(value: T?, parcel: Parcel, flags: Int) {
    if (value == null) {
        parcel.writeBooleanCompat(false)
    } else {
        parcel.writeBooleanCompat(true)
        value.write(parcel, flags)
    }
}

internal fun <T> Parceler<T>.createNullable(parcel: Parcel): T? {
    return if (!parcel.readBooleanCompat()) {
        null
    } else {
        this.create(parcel)
    }
}

internal fun <T> Parceler<T>.write(value: T, parcel: Parcel, flags: Int) {
    value.write(parcel, flags)
}

internal fun Parcel.writeBooleanCompat(value: Boolean) {
    return this.writeInt(if (value) 1 else 0)
}

internal fun Parcel.readBooleanCompat(): Boolean {
    return this.readInt() != 0
}

internal fun <L, R> Parcel.writeEither(either: Either<L, R>, writeLeft: (L) -> Unit, writeRight: (R) -> Unit) {
    when (either) {
        is Either.Left -> {
            writeBooleanCompat(false)
            writeLeft(either.a)
        }
        is Either.Right -> {
            writeBooleanCompat(true)
            writeRight(either.b)
        }
    }.exhaust()
}

internal fun <L, R> Parcel.readEither(readLeft: () -> L, readRight: () -> R): Either<L, R> {
    return if (readBooleanCompat()) {
        readRight().right()
    } else {
        readLeft().left()
    }
}

internal fun <L, R> Parcel.writeEitherNullable(either: Either<L, R>?, writeLeft: (L) -> Unit, writeRight: (R) -> Unit) {
    if (either == null) {
        writeBooleanCompat(false)
    } else {
        writeBooleanCompat(true)
        writeEither(either, writeLeft, writeRight)
    }
}

internal fun <L, R> Parcel.readEitherNullable(readLeft: () -> L, readRight: () -> R): Either<L, R>? {
    return if (readBooleanCompat()) {
        readEither(readLeft, readRight)
    } else {
        null
    }
}
