package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.common.Name
import kotlinx.android.parcel.Parceler

internal object NameParceler : Parceler<Name> {

    override fun create(parcel: Parcel): Name {
        return Name.of(parcel.readString()!!).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun Name.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this.value)
    }
}
