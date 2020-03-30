package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath
import kotlinx.android.parcel.Parceler

internal object PicturePathParceler : Parceler<PicturePath> {

    override fun create(parcel: Parcel): PicturePath {
        return PicturePath.of(parcel.readString()!!).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun PicturePath.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this.value)
    }
}
