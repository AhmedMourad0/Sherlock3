package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.domain.model.common.Url
import kotlinx.android.parcel.Parceler

internal object UrlParceler : Parceler<Url> {

    override fun create(parcel: Parcel): Url {
        return Url.of(parcel.readString()!!).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun Url.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this.value)
    }
}
