package dev.ahmedmourad.sherlock.android.utils.pickers.images

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.features.ReturnMode
import dev.ahmedmourad.sherlock.android.R
import splitties.init.appCtx
import javax.inject.Inject
import com.esafirm.imagepicker.features.ImagePicker as DelegateImagePicker

private typealias StartActivityForResult = (Intent, Int) -> Unit

internal class EsafirmImagePicker @Inject constructor() : ImagePicker {

    private val requestCode = (0..65535).random()

    override fun start(activity: Activity, onError: OnError) {
        start(DelegateImagePicker.create(activity),
                startActivityForResult = activity::startActivityForResult,
                onError = onError
        )
    }

    override fun start(fragment: Fragment, onError: OnError) {
        start(DelegateImagePicker.create(fragment),
                startActivityForResult = fragment::startActivityForResult,
                onError = onError
        )
    }

    private fun start(
            imagePicker: DelegateImagePicker,
            startActivityForResult: StartActivityForResult,
            onError: OnError
    ) {
        try {
            startActivityForResult(imagePicker.returnMode(ReturnMode.ALL)
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
