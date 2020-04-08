package inc.ahmedmourad.sherlock.model.children

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import inc.ahmedmourad.sherlock.domain.model.EitherSerializer
import inc.ahmedmourad.sherlock.domain.model.children.PublishedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.ApproximateAppearance
import inc.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.domain.model.common.Name
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath
import inc.ahmedmourad.sherlock.utils.getImageBytes
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error

@Serializable
internal class AppPublishedChild private constructor(
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val picturePath: PicturePath?
) {

    fun toPublishedChild(): PublishedChild {
        return PublishedChild.of(
                name,
                notes,
                location,
                appearance,
                getImageBytes(picturePath)
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    fun component1() = name

    fun component2() = notes

    fun component3() = location

    fun component4() = appearance

    fun component5() = picturePath

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as AppPublishedChild

        if (name != other.name)
            return false

        if (notes != other.notes)
            return false

        if (location != other.location)
            return false

        if (appearance != other.appearance)
            return false

        if (picturePath != other.picturePath)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + appearance.hashCode()
        result = 31 * result + picturePath.hashCode()
        return result
    }

    override fun toString(): String {
        return "AppPublishedChild(" +
                "name=$name, " +
                "notes=$notes, " +
                "location=$location, " +
                "appearance=$appearance, " +
                "picturePath=$picturePath" +
                ")"
    }

    companion object {
        fun of(name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               picturePath: PicturePath?
        ): Either<PublishedChild.Exception, AppPublishedChild> {
            return PublishedChild.validate(
                    name,
                    notes,
                    location,
                    appearance,
                    getImageBytes(picturePath)
            )?.left() ?: AppPublishedChild(name, notes, location, appearance, picturePath).right()
        }
    }
}
