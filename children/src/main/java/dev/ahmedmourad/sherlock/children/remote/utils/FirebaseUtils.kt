package dev.ahmedmourad.sherlock.children.remote.utils

import arrow.core.Either
import com.google.firebase.firestore.FieldValue
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url

fun PublishedChild.toMap(pictureUrl: Url?): Map<String, Any?> = hashMapOf(
        Contract.Database.Children.PUBLICATION_DATE to FieldValue.serverTimestamp(),
        Contract.Database.Children.LOCATION_ID to location?.id,
        Contract.Database.Children.LOCATION_NAME to location?.name,
        Contract.Database.Children.LOCATION_ADDRESS to location?.address,
        Contract.Database.Children.LOCATION_LATITUDE to location?.coordinates?.latitude,
        Contract.Database.Children.LOCATION_LONGITUDE to location?.coordinates?.longitude,
        Contract.Database.Children.MIN_AGE to appearance.ageRange?.min?.value,
        Contract.Database.Children.MAX_AGE to appearance.ageRange?.max?.value,
        Contract.Database.Children.MIN_HEIGHT to appearance.heightRange?.min?.value,
        Contract.Database.Children.MAX_HEIGHT to appearance.heightRange?.max?.value,
        Contract.Database.Children.GENDER to appearance.gender?.value,
        Contract.Database.Children.SKIN to appearance.skin?.value,
        Contract.Database.Children.HAIR to appearance.hair?.value,
        Contract.Database.Children.NOTES to notes,
        Contract.Database.Children.PICTURE_URL to pictureUrl?.value
) + createNameHashMap(name)

private fun createNameHashMap(name: Either<Name, FullName>?): Map<String, Any?> {

    if (name == null) {
        return hashMapOf(
                Contract.Database.Children.FIRST_NAME to null,
                Contract.Database.Children.LAST_NAME to null
        )
    }

    return name.fold(ifLeft = {
        hashMapOf(
                Contract.Database.Children.FIRST_NAME to it.value,
                Contract.Database.Children.LAST_NAME to null
        )
    }, ifRight = {
        hashMapOf(
                Contract.Database.Children.FIRST_NAME to it.first.value,
                Contract.Database.Children.LAST_NAME to it.last.value
        )
    })
}
