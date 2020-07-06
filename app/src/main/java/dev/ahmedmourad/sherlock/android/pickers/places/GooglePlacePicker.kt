package dev.ahmedmourad.sherlock.android.pickers.places

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import splitties.init.appCtx
import javax.inject.Inject
import com.google.android.gms.location.places.ui.PlacePicker as DelegatePlacePicker

internal class GooglePlacePicker @Inject constructor() : PlacePicker {

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
        try {
            startActivityForResult(DelegatePlacePicker.IntentBuilder().build(activity), requestCode)
        } catch (e: GooglePlayServicesRepairableException) {
            onError(e)
        } catch (e: GooglePlayServicesNotAvailableException) {
            onError(e)
        }
    }

    override fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect) {
        if (requestCode == this.requestCode) {

            val place = DelegatePlacePicker.getPlace(appCtx, data) ?: return

            onSelect(PlacePicker.Location(
                    place.id,
                    place.name.toString(),
                    place.address.toString(),
                    place.latLng.latitude,
                    place.latLng.longitude
            ))
        }
    }
}
