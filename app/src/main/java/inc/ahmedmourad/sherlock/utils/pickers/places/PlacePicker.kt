package inc.ahmedmourad.sherlock.utils.pickers.places

import android.app.Activity
import android.content.Intent

internal typealias OnError = (Throwable) -> Unit
internal typealias OnSelect = (PlacePicker.Location) -> Unit

internal interface PlacePicker {

    fun start(activity: Activity, onError: OnError = { })

    fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect)

    data class Location(
            val id: String,
            val name: String,
            val address: String,
            val latitude: Double,
            val longitude: Double
    )
}
