package dev.ahmedmourad.sherlock.android.model.children

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
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
internal data class AppChildToPublish private constructor(
        val user: SimpleRetrievedUser,
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val picturePath: PicturePath?
) {

    fun toChildToPublish(imageLoader: ImageLoader): ChildToPublish {
        return ChildToPublish.of(
                user,
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
        fun of(user: SimpleRetrievedUser,
               name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               picturePath: PicturePath?,
               imageLoader: ImageLoader
        ): Either<ChildToPublish.Exception, AppChildToPublish> {
            return ChildToPublish.validate(
                    user,
                    name,
                    notes,
                    location,
                    appearance,
                    imageLoader.getBytesOrNull(picturePath?.value)
            )?.left()
                    ?: AppChildToPublish(user, name, notes, location, appearance, picturePath).right()
        }
    }
}
