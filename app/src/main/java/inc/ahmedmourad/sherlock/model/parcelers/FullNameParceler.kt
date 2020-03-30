package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import inc.ahmedmourad.sherlock.model.parcelers.utils.write
import kotlinx.android.parcel.Parceler

internal object FullNameParceler : Parceler<FullName> {

    override fun create(parcel: Parcel): FullName {
        return FullName.of(
                NameParceler.create(parcel),
                NameParceler.create(parcel)
        ).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun FullName.write(parcel: Parcel, flags: Int) {
        NameParceler.write(this.first, parcel, flags)
        NameParceler.write(this.last, parcel, flags)
    }
}
