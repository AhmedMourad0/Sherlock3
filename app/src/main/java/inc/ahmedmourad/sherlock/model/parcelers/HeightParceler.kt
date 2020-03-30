package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Height
import kotlinx.android.parcel.Parceler

internal object HeightParceler : Parceler<Height> {

    override fun create(parcel: Parcel): Height {
        return Height.of(parcel.readInt()).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun Height.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.value)
    }
}
