package inc.ahmedmourad.sherlock.utils.pickers.images

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

internal typealias OnSelect = (ImagePicker.PicturePath) -> Unit
internal typealias OnError = (Throwable) -> Unit

internal interface ImagePicker {

    fun start(activity: Activity, onError: OnError = { })

    fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect)

    @Parcelize
    data class PicturePath(val value: String) : Parcelable
}
