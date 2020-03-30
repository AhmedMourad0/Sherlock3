package inc.ahmedmourad.sherlock.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath
import splitties.init.appCtx
import java.io.ByteArrayOutputStream

internal fun getImageBytes(imagePath: PicturePath?, @DrawableRes onError: Int = R.drawable.placeholder): ByteArray {
    return if (imagePath == null) {
        getImageBytes(onError)
    } else {
        getImageBytes(getImageBitmap(imagePath.value, onError))
    }
}

fun getImageBytes(bitmap: Bitmap): ByteArray {
    return ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }.toByteArray()
}

private fun getImageBytes(@DrawableRes drawablePath: Int): ByteArray {
    return getImageBytes((ContextCompat.getDrawable(appCtx, drawablePath) as BitmapDrawable).bitmap)
}

fun getImageBitmap(imagePath: String, @DrawableRes onError: Int): Bitmap {
    return Glide.with(appCtx)
            .asBitmap()
            .load(imagePath)
            .error(onError)
            .submit()
            .get()
}
