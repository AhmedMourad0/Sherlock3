package inc.ahmedmourad.sherlock.utils.pickers.places

import android.app.Activity
import android.content.Intent
import arrow.core.Either
import arrow.core.extensions.fx
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import inc.ahmedmourad.sherlock.validators.children.validateCoordinates
import inc.ahmedmourad.sherlock.validators.children.validateLocation
import splitties.init.appCtx
import com.google.android.gms.location.places.ui.PlacePicker as DelegatePlacePicker

internal class GooglePlacePicker : PlacePicker {

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

    override fun handleActivityResult(requestCode: Int, data: Intent, onHandled: OnHandled) {
        if (requestCode == this.requestCode) {

            val place = DelegatePlacePicker.getPlace(appCtx, data) ?: return

            onHandled(Either.fx {

                val (coordinates) = validateCoordinates(place.latLng.latitude, place.latLng.longitude)

                validateLocation(place.id,
                        place.name.toString(),
                        place.address.toString(),
                        coordinates
                ).bind()
            })
        }
    }
}
