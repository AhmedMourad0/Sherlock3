package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.AgeRange
import inc.ahmedmourad.sherlock.model.parcelers.utils.write
import kotlinx.android.parcel.Parceler

internal object AgeRangeParceler : Parceler<AgeRange> {

    override fun create(parcel: Parcel): AgeRange {
        return AgeRange.of(
                AgeParceler.create(parcel),
                AgeParceler.create(parcel)
        ).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun AgeRange.write(parcel: Parcel, flags: Int) {
        AgeParceler.write(this.min, parcel, flags)
        AgeParceler.write(this.max, parcel, flags)
    }
}
