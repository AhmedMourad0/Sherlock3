package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.findEnum
import kotlinx.android.parcel.Parceler

internal object GenderParceler : Parceler<Gender> {

    override fun create(parcel: Parcel): Gender {
        return findEnum(parcel.readInt(), Gender.values())
    }

    override fun Gender.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.value)
    }
}
