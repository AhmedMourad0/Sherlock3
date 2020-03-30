package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.ExactAppearance
import inc.ahmedmourad.sherlock.model.parcelers.utils.write
import kotlinx.android.parcel.Parceler

internal object ExactAppearanceParceler : Parceler<ExactAppearance> {

    override fun create(parcel: Parcel): ExactAppearance {
        return ExactAppearance.of(
                GenderParceler.create(parcel),
                SkinParceler.create(parcel),
                HairParceler.create(parcel),
                AgeParceler.create(parcel),
                HeightParceler.create(parcel)
        ).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun ExactAppearance.write(parcel: Parcel, flags: Int) {
        GenderParceler.write(this.gender, parcel, flags)
        SkinParceler.write(this.skin, parcel, flags)
        HairParceler.write(this.hair, parcel, flags)
        AgeParceler.create(parcel)
        HeightParceler.create(parcel)
    }
}
