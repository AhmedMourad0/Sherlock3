package dev.ahmedmourad.sherlock.android.pickers.images

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.fragment.app.Fragment
import dev.ahmedmourad.sherlock.domain.model.common.Url
import kotlinx.android.parcel.Parcelize
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath as DomainPicturePath

internal typealias OnSelect = (ImagePicker.PicturePath) -> Unit
internal typealias OnError = (Throwable) -> Unit

internal interface ImagePicker {

    fun start(activity: Activity, onError: OnError = { })

    fun start(fragment: Fragment, onError: OnError = { })

    fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect)

    @Parcelize
    data class PicturePath(val value: String) : Parcelable {
        companion object {

            fun from(picturePath: DomainPicturePath): PicturePath {
                return PicturePath(picturePath.value)
            }

            fun from(url: Url): PicturePath {
                return PicturePath(url.value)
            }
        }
    }
}
