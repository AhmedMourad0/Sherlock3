package inc.ahmedmourad.sherlock.utils.pickers.places

import android.app.Activity
import android.content.Intent
import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location

internal typealias OnError = (Throwable) -> Unit
internal typealias OnHandled = (Either<String, Location>) -> Unit

internal interface PlacePicker {

    fun start(activity: Activity, onError: OnError = { })

    fun handleActivityResult(requestCode: Int, data: Intent, onHandled: OnHandled)
}
