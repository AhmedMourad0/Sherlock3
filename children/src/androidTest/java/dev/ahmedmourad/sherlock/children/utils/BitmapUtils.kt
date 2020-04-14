package dev.ahmedmourad.sherlock.children.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import splitties.init.appCtx
import java.io.ByteArrayOutputStream

fun getImageBytes(bitmap: Bitmap): ByteArray {
    return ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }.toByteArray()
}

fun getImageBytes(@DrawableRes drawablePath: Int): ByteArray {
    return getImageBytes((ContextCompat.getDrawable(appCtx, drawablePath) as BitmapDrawable).bitmap)
}
