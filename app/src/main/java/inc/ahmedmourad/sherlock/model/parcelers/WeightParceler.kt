package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import kotlinx.android.parcel.Parceler

internal object WeightParceler : Parceler<Weight> {

    override fun create(parcel: Parcel): Weight {
        return Weight.of(parcel.readDouble()).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun Weight.write(parcel: Parcel, flags: Int) {
        parcel.writeDouble(this.value)
    }
}
