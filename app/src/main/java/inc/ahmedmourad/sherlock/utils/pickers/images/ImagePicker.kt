package inc.ahmedmourad.sherlock.utils.pickers.images

import android.app.Activity
import android.content.Intent
import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath

internal typealias OnHandled = (Either<String, PicturePath>) -> Unit
internal typealias OnError = (Throwable) -> Unit

internal interface ImagePicker {

    fun start(activity: Activity, onError: OnError = { })

    fun handleActivityResult(requestCode: Int, data: Intent, onHandled: OnHandled)
}
