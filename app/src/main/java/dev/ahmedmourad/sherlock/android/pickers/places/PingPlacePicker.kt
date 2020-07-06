package dev.ahmedmourad.sherlock.android.pickers.places

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import dev.ahmedmourad.sherlock.android.R
import javax.inject.Inject
import com.rtchagas.pingplacepicker.PingPlacePicker as DelegatePlacePicker

internal class PingPlacePicker @Inject constructor() : PlacePicker {

    private val requestCode = (0..65535).random()

    override fun start(activity: Activity, onError: OnError) {
        start(activity,
                startActivityForResult = activity::startActivityForResult,
                onError = onError
        )
    }

    override fun start(fragment: Fragment, onError: OnError) {
        start(fragment.requireActivity(),
                startActivityForResult = fragment::startActivityForResult,
                onError = onError
        )
    }

    private fun start(
            activity: Activity,
            startActivityForResult: StartActivityForResult,
            onError: OnError
    ) {
        val builder = DelegatePlacePicker.IntentBuilder()
        builder.setAndroidApiKey(activity.getString(R.string.google_places_api_key))
                .setMapsApiKey(activity.getString(R.string.google_places_api_key))

        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        // builder.setLatLng(LatLng(37.4219999, -122.0862462))

        try {
            startActivityForResult(builder.build(activity), requestCode)
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    override fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect) {
        if (requestCode == this.requestCode) {
            val place = DelegatePlacePicker.getPlace(data) ?: return
            onSelect(PlacePicker.Location(
                    place.id,
                    place.name,
                    place.address,
                    place.latLng?.latitude,
                    place.latLng?.longitude
            ))
        }
    }
}
