package dev.ahmedmourad.sherlock.android.utils.pickers.images

import android.app.Activity
import android.content.Intent
import com.esafirm.imagepicker.features.ReturnMode
import dev.ahmedmourad.sherlock.android.R
import splitties.init.appCtx
import javax.inject.Inject
import com.esafirm.imagepicker.features.ImagePicker as DelegateImagePicker

internal class EsafirmImagePicker @Inject constructor() : ImagePicker {

    private val requestCode = (0..Int.MAX_VALUE).random()

    override fun start(activity: Activity, onError: OnError) {
        try {
            activity.startActivityForResult(DelegateImagePicker.create(activity)
                    .returnMode(ReturnMode.ALL)
                    .folderMode(true)
                    .toolbarFolderTitle(appCtx.getString(R.string.pick_a_folder))
                    .toolbarImageTitle(appCtx.getString(R.string.tap_to_select))
                    .single()
                    .limit(1)
                    .showCamera(true)
                    .imageDirectory(appCtx.getString(R.string.image_directory))
                    .theme(R.style.ImagePickerTheme)
                    .enableLog(true)
                    .getIntent(appCtx), requestCode
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect) {
        if (requestCode == this.requestCode) {
            val path = DelegateImagePicker.getFirstImageOrNull(data)?.path ?: return
            onSelect(ImagePicker.PicturePath(path))
        }
    }
}
