package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.model.parcelers.utils.write
import kotlinx.android.parcel.Parceler

internal object LocationParceler : Parceler<Location> {

    override fun create(parcel: Parcel): Location {
        return Location.of(
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!,
                CoordinatesParceler.create(parcel)
        ).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun Location.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this.id)
        parcel.writeString(this.name)
        parcel.writeString(this.address)
        CoordinatesParceler.write(this.coordinates, parcel, flags)
    }
}
