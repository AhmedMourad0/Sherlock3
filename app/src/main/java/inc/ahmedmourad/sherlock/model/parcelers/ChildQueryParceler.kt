package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.model.parcelers.utils.write
import kotlinx.android.parcel.Parceler

internal object ChildQueryParceler : Parceler<ChildQuery> {

    override fun create(parcel: Parcel): ChildQuery {
        return ChildQuery.of(
                FullNameParceler.create(parcel),
                LocationParceler.create(parcel),
                ExactAppearanceParceler.create(parcel)
        ).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun ChildQuery.write(parcel: Parcel, flags: Int) {
        FullNameParceler.write(this.fullName, parcel, flags)
        LocationParceler.write(this.location, parcel, flags)
        ExactAppearanceParceler.write(this.appearance, parcel, flags)
    }
}
