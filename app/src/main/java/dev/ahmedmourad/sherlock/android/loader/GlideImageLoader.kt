package dev.ahmedmourad.sherlock.android.loader

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import arrow.core.Either
import arrow.core.left
import arrow.core.orNull
import arrow.core.right
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.Reusable
import splitties.init.appCtx
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@Reusable
internal class GlideImageLoader @Inject constructor() : ImageLoader {

    private val glide by lazy { Glide.with(appCtx) }

    override fun load(
            src: String?,
            target: ImageView,
            @DrawableRes placeholder: Int,
            @DrawableRes error: Int
    ) {
        glide.load(src)
                .placeholder(error)
                .error(placeholder)
                .into(target)
    }

    override fun fetch(src: String?): Either<Throwable, Bitmap> {
        return try {
            glide.asBitmap()
                    .load(src)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .submit()
                    .get()
                    .right()
        } catch (e: Exception) {
            e.left()
        }
    }

    override fun getBytesOrNull(src: String?): ByteArray? {

        if (src == null) {
            return null
        }

        return getBytesOrNull(fetch(src).orNull())
    }

    override fun getBytesOrNull(src: Bitmap?): ByteArray? {

        if (src == null) {
            return null
        }

        return ByteArrayOutputStream().also {
            src.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }.toByteArray()
    }
}
