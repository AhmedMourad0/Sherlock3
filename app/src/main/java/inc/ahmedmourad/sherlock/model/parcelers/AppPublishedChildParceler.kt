package inc.ahmedmourad.sherlock.model.parcelers

import android.os.Parcel
import arrow.core.getOrHandle
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.model.parcelers.utils.*
import kotlinx.android.parcel.Parceler

internal object AppPublishedChildParceler : Parceler<AppPublishedChild> {

    override fun create(parcel: Parcel): AppPublishedChild {
        return AppPublishedChild.of(
                parcel.readEitherNullable({ NameParceler.create(parcel) }, { FullNameParceler.create(parcel) }),
                parcel.readString(),
                LocationParceler.createNullable(parcel),
                ApproximateAppearanceParceler.create(parcel),
                PicturePathParceler.createNullable(parcel)
        ).getOrHandle {
            throw ModelCreationException(it.toString())
        }
    }

    override fun AppPublishedChild.write(parcel: Parcel, flags: Int) {
        parcel.writeEitherNullable(
                this.name,
                { NameParceler.write(it, parcel, flags) },
                { FullNameParceler.write(it, parcel, flags) }
        )
        parcel.writeString(this.notes)
        LocationParceler.writeNullable(this.location, parcel, flags)
        ApproximateAppearanceParceler.write(this.appearance, parcel, flags)
        PicturePathParceler.writeNullable(this.picturePath, parcel, flags)
    }
}
