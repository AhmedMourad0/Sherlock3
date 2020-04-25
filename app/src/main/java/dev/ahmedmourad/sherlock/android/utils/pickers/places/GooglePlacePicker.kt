package dev.ahmedmourad.sherlock.android.utils.pickers.places

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import splitties.init.appCtx
import javax.inject.Inject
import com.google.android.gms.location.places.ui.PlacePicker as DelegatePlacePicker

internal class GooglePlacePicker @Inject constructor() : PlacePicker {

    private val requestCode = (0..Int.MAX_VALUE).random()

    override fun start(activity: Activity, onError: OnError) {

        try {
            activity.startActivityForResult(DelegatePlacePicker.IntentBuilder().build(activity), requestCode)
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
