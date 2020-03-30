package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Age
import kotlinx.android.parcel.Parceler

internal object AgeParceler : Parceler<Age> {

    override fun create(parcel: Parcel): Age {
        return Age.of(parcel.readInt()).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun Age.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.value)
    }
}
