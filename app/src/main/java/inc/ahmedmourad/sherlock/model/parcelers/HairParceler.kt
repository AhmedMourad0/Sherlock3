package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.findEnum
import kotlinx.android.parcel.Parceler

internal object HairParceler : Parceler<Hair> {

    override fun create(parcel: Parcel): Hair {
        return findEnum(parcel.readInt(), Hair.values())
    }

    override fun Hair.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.value)
    }
}
