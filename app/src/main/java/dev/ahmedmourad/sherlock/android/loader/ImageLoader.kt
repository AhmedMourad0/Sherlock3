package dev.ahmedmourad.sherlock.android.loader

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import arrow.core.Either

internal interface ImageLoader {

    fun load(
            src: String?,
            target: ImageView,
            @DrawableRes placeholder: Int,
            @DrawableRes error: Int
    )

    fun fetch(src: String?): Either<Throwable, Bitmap>

    fun getBytesOrNull(src: String?): ByteArray?

    fun getBytesOrNull(src: Bitmap?): ByteArray?
}
