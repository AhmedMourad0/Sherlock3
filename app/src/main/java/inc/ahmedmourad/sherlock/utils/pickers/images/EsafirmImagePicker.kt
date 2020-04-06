package inc.ahmedmourad.sherlock.utils.pickers.images

import android.app.Activity
import android.content.Intent
import com.esafirm.imagepicker.features.ReturnMode
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.model.validators.common.validatePicturePath
import splitties.init.appCtx
import com.esafirm.imagepicker.features.ImagePicker as DelegateImagePicker

internal class EsafirmImagePicker : ImagePicker {

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

    override fun handleActivityResult(requestCode: Int, data: Intent, onHandled: OnHandled) {
        if (requestCode == this.requestCode) {
            val path = DelegateImagePicker.getFirstImageOrNull(data)?.path ?: return
            onHandled(validatePicturePath(path))
        }
    }
}
