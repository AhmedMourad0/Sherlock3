package dev.ahmedmourad.sherlock.android.model.children

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.ApproximateAppearance
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Location
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error

@Serializable
@NoCopy
internal data class AppPublishedChild private constructor(
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val picturePath: PicturePath?
) {

    fun toPublishedChild(imageLoader: ImageLoader): PublishedChild {
        return PublishedChild.of(
                name,
                notes,
                location,
                appearance,
                imageLoader.getBytesOrNull(picturePath?.value)
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    companion object {
        fun of(name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               picturePath: PicturePath?,
               imageLoader: ImageLoader
        ): Either<PublishedChild.Exception, AppPublishedChild> {
            return PublishedChild.validate(
                    name,
                    notes,
                    location,
                    appearance,
                    imageLoader.getBytesOrNull(picturePath?.value)
            )?.left() ?: AppPublishedChild(name, notes, location, appearance, picturePath).right()
        }
    }
}
