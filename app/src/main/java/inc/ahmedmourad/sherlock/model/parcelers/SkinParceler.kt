package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.constants.findEnum
import kotlinx.android.parcel.Parceler

internal object SkinParceler : Parceler<Skin> {

    override fun create(parcel: Parcel): Skin {
        return findEnum(parcel.readInt(), Skin.values())
    }

    override fun Skin.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.value)
    }
}
