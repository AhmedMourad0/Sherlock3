package dev.ahmedmourad.sherlock.android.utils.pickers.places

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Location as DomainLocation

internal typealias OnError = (Throwable) -> Unit
internal typealias OnSelect = (PlacePicker.Location) -> Unit

internal interface PlacePicker {

    fun start(activity: Activity, onError: OnError = { })

    fun handleActivityResult(requestCode: Int, data: Intent, onSelect: OnSelect)

    @Parcelize
    data class Location(
            val id: String,
            val name: String,
            val address: String,
            val latitude: Double,
            val longitude: Double
    ) : Parcelable {
        companion object {
            fun from(location: DomainLocation): Location {
                return Location(
                        location.id,
                        location.name,
                        location.address,
                        location.coordinates.latitude,
                        location.coordinates.longitude
                )
            }
        }
    }
}
