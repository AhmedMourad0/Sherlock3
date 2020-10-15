package dev.ahmedmourad.sherlock.children.local.daos

import arrow.core.*
import arrow.core.extensions.fx
import com.squareup.sqldelight.runtime.rx.asObservable
import com.squareup.sqldelight.runtime.rx.mapToList
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.local.ChildrenQueries
import dev.ahmedmourad.sherlock.children.local.UsersQueries
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

interface ChildrenDao {

    fun replaceAll(
            items: Map<SimpleRetrievedChild, Weight>
    ): Single<Map<SimpleRetrievedChild, Weight>>

    fun findAllSimpleWhereWeightExists(): Flowable<Map<SimpleRetrievedChild, Weight>>

    fun findById(id: ChildId): Flowable<Option<Tuple2<RetrievedChild, Weight?>>>

    fun insertOrReplaceRetainingWeight(
            item: RetrievedChild
    ): Flowable<Tuple2<RetrievedChild, Weight?>>
}

@Reusable
internal class ChildrenDaoImpl @Inject constructor(
        @InternalApi private val childrenQueries: ChildrenQueries,
        @InternalApi private val usersQueries: UsersQueries
) : ChildrenDao {

    override fun replaceAll(
            items: Map<SimpleRetrievedChild, Weight>
    ): Single<Map<SimpleRetrievedChild, Weight>> {
        return Completable.fromAction {

            childrenQueries.transaction {
                childrenQueries.deleteAll()
                usersQueries.deleteAll()
                items.forEach { (child, weight) ->

                    val (firstName, lastName) = child.name?.fold(ifLeft = {
                        it.value to null
                    }, ifRight = {
                        it.first.value to it.last.value
                    }) ?: null to null

                    usersQueries.insertOrReplaceSimple(
                            id = child.user.id.value,
                            display_name = child.user.displayName.value,
                            picture_url = child.user.pictureUrl?.value
                    )

                    childrenQueries.insertOrReplaceSimple(
                            id = child.id.value,
                            user_id = child.user.id.value,
                            timestamp = child.timestamp,
                            first_name = firstName,
                            last_name = lastName,
                            location_name = child.locationName,
                            location_address = child.locationAddress,
                            picture_url = child.pictureUrl?.value,
                            weight = weight.value,
                            notes = child.notes
                    )
                }
            }
        }.toSingleDefault(items)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
    }

    override fun findAllSimpleWhereWeightExists(): Flowable<Map<SimpleRetrievedChild, Weight>> {
        return childrenQueries.findAllSimpleWhereWeightExists { childId,
                                                                childTimestamp,
                                                                childFirstName,
                                                                childLastName,
                                                                childLocationName,
                                                                childLocationAddress,
                                                                childPictureUrl,
                                                                childWeight,
                                                                childNotes,
                                                                userId,
                                                                userDisplayName,
                                                                userPictureUrl ->

            Either.fx<ModelCreationException, Pair<SimpleRetrievedChild, Weight>> {

                val uDisplayName = DisplayName.of(userDisplayName)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()

                val uPictureUrl = userPictureUrl?.let(Url::of)
                        ?.mapLeft { ModelCreationException(it.toString()) }
                        ?.bind()

                val user = SimpleRetrievedUser.of(
                        id = UserId(userId),
                        displayName = uDisplayName,
                        pictureUrl = uPictureUrl
                ).right().bind()

                val cName = parseName(childFirstName, childLastName).bind()

                val cPictureUrl = childPictureUrl?.let(Url::of)
                        ?.mapLeft { ModelCreationException(it.toString()) }
                        ?.bind()

                val cWeight = Weight.of(childWeight!!)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()

                SimpleRetrievedChild.of(
                        id = ChildId(childId),
                        user = user,
                        timestamp = childTimestamp,
                        name = cName,
                        notes = childNotes,
                        locationName = childLocationName,
                        locationAddress = childLocationAddress,
                        pictureUrl = cPictureUrl
                ).bimap(leftOperation = {
                    ModelCreationException(it.toString())
                }, rightOperation = {
                    it to cWeight
                }).bind()
            }
        }.asObservable()
                .mapToList()
                .map {
                    it.mapNotNull(Either<ModelCreationException, Pair<SimpleRetrievedChild, Weight>>::orNull)
                            .toMap()
                }.subscribeOn(Schedulers.io())
                .toFlowable(BackpressureStrategy.LATEST)
    }

    override fun findById(id: ChildId): Flowable<Option<Tuple2<RetrievedChild, Weight?>>> {
        return childrenQueries.findChildById(id.value) { childTimestamp,
                                                         childFirstName,
                                                         childLastName,
                                                         childPictureUrl,
                                                         childLocationId,
                                                         childLocationName,
                                                         childLocationAddress,
                                                         childLocationLatitude,
                                                         childLocationLongitude,
                                                         childGender,
                                                         childSkin,
                                                         childHair,
                                                         childMinAge,
                                                         childMaxAge,
                                                         childMinHeight,
                                                         childMaxHeight,
                                                         childWeight,
                                                         childNotes,
                                                         userId,
                                                         userDisplayName,
                                                         userPictureUrl ->

            Either.fx<ModelCreationException, Tuple2<RetrievedChild, Weight?>> {

                val uDisplayName = DisplayName.of(userDisplayName)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()

                val uPictureUrl = userPictureUrl?.let(Url::of)
                        ?.mapLeft { ModelCreationException(it.toString()) }
                        ?.bind()

                val user = SimpleRetrievedUser.of(
                        id = UserId(userId),
                        displayName = uDisplayName,
                        pictureUrl = uPictureUrl
                ).right().bind()

                val cName = parseName(childFirstName, childLastName).bind()

                val cPictureUrl = childPictureUrl?.let(Url::of)
                        ?.mapLeft { ModelCreationException(it.toString()) }
                        ?.bind()

                val cLocation = parseLocation(
                        childLocationId,
                        childLocationName,
                        childLocationAddress,
                        childLocationLatitude,
                        childLocationLongitude
                ).bind()

                val cAppearance = parseAppearance(
                        childGender?.toInt(),
                        childSkin?.toInt(),
                        childHair?.toInt(),
                        childMinAge?.toInt(),
                        childMaxAge?.toInt(),
                        childMinHeight?.toInt(),
                        childMaxHeight?.toInt()
                ).bind()

                val cWeight = childWeight?.let(Weight::of)
                        ?.mapLeft { ModelCreationException(it.toString()) }
                        ?.bind()

                RetrievedChild.of(
                        id = id,
                        user = user,
                        timestamp = childTimestamp,
                        name = cName,
                        notes = childNotes,
                        location = cLocation,
                        appearance = cAppearance,
                        pictureUrl = cPictureUrl
                ).bimap(leftOperation = {
                    ModelCreationException(it.toString())
                }, rightOperation = {
                    it toT cWeight
                }).bind()
            }
        }.asObservable()
                .mapToList()
                .map { l ->
                    Timber.error { l.toString() }
                    l.firstOrNull()?.mapLeft {
                        Timber.error(it, it::toString)
                        it
                    }?.orNull().toOption()
                }.subscribeOn(Schedulers.io())
                .toFlowable(BackpressureStrategy.LATEST)
    }

    private fun parseName(
            first: String?,
            last: String?
    ): Either<ModelCreationException, Either<Name, FullName>?> {
        return Either.fx {
            when {

                first == null -> null

                last == null -> {
                    Name.of(first)
                            .mapLeft { ModelCreationException(it.toString()) }
                            .bind()
                            .left()
                }

                else -> {

                    val f = Name.of(first)
                            .mapLeft { ModelCreationException(it.toString()) }
                            .bind()

                    val l = Name.of(last)
                            .mapLeft { ModelCreationException(it.toString()) }
                            .bind()

                    FullName.of(f, l).right()
                }
            }
        }
    }

    private fun parseLocation(
            id: String?,
            name: String?,
            address: String?,
            latitude: Double?,
            longitude: Double?
    ): Either<ModelCreationException, Location?> {
        return Either.fx {

            latitude ?: return@fx null.right().bind()
            longitude ?: return@fx null.right().bind()

            val coordinates = Coordinates.of(latitude, longitude)
                    .mapLeft { ModelCreationException(it.toString()) }
                    .bind()

            Location.of(
                    id,
                    name,
                    address,
                    coordinates
            ).right().bind()
        }
    }

    private fun parseAppearance(
            gender: Int?,
            skin: Int?,
            hair: Int?,
            minAge: Int?,
            maxAge: Int?,
            minHeight: Int?,
            maxHeight: Int?
    ): Either<ModelCreationException, ApproximateAppearance> {
        return Either.fx {

            val g = gender?.let { findEnum(it, Gender.values()) }
            val s = skin?.let { findEnum(it, Skin.values()) }
            val h = hair?.let { findEnum(it, Hair.values()) }

            val minAgeWrapped = minAge?.let(Age::of)
                    ?.mapLeft { ModelCreationException(it.toString()) }
                    ?.bind()

            val maxAgeWrapped = maxAge?.let(Age::of)
                    ?.mapLeft { ModelCreationException(it.toString()) }
                    ?.bind()

            val age = if (minAgeWrapped == null || maxAgeWrapped == null) {
                null
            } else {
                AgeRange.of(minAgeWrapped, maxAgeWrapped)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()
            }

            val minHeightWrapped = minHeight?.let(Height::of)
                    ?.mapLeft { ModelCreationException(it.toString()) }
                    ?.bind()

            val maxHeightWrapped = maxHeight?.let(Height::of)
                    ?.mapLeft { ModelCreationException(it.toString()) }
                    ?.bind()

            val height = if (minHeightWrapped == null || maxHeightWrapped == null) {
                null
            } else {
                HeightRange.of(minHeightWrapped, maxHeightWrapped)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()
            }

            ApproximateAppearance.of(
                    g,
                    s,
                    h,
                    age,
                    height
            ).mapLeft { ModelCreationException(it.toString()) }.bind()
        }
    }

    override fun insertOrReplaceRetainingWeight(
            item: RetrievedChild
    ): Flowable<Tuple2<RetrievedChild, Weight?>> {
        return Completable.fromAction {

            val (firstName, lastName) = item.name?.fold(ifLeft = {
                it.value to null
            }, ifRight = {
                it.first.value to it.last.value
            }) ?: null to null

            childrenQueries.transaction {

                val user = usersQueries.findUserById(item.user.id.value).executeAsOneOrNull()

                if (user == null) {
                    usersQueries.insertOrReplaceSimple(
                            id = item.user.id.value,
                            display_name = item.user.displayName.value,
                            picture_url = item.user.pictureUrl?.value
                    )
                } else {
                    usersQueries.updateSimple(
                            id = item.user.id.value,
                            display_name = item.user.displayName.value,
                            picture_url = item.user.pictureUrl?.value
                    )
                }

                val weight = childrenQueries.findWeight(item.id.value).executeAsOneOrNull()?.weight

                childrenQueries.insertOrReplace(
                        id = item.id.value,
                        user_id = item.user.id.value,
                        timestamp = item.timestamp,
                        first_name = firstName,
                        last_name = lastName,
                        location_id = item.location?.id,
                        location_name = item.location?.name,
                        location_address = item.location?.address,
                        location_latitude = item.location?.coordinates?.latitude,
                        location_longitude = item.location?.coordinates?.longitude,
                        gender = item.appearance.gender?.value?.toLong(),
                        skin = item.appearance.skin?.value?.toLong(),
                        hair = item.appearance.hair?.value?.toLong(),
                        min_age = item.appearance.ageRange?.min?.value?.toLong(),
                        max_age = item.appearance.ageRange?.max?.value?.toLong(),
                        min_height = item.appearance.heightRange?.min?.value?.toLong(),
                        max_height = item.appearance.heightRange?.max?.value?.toLong(),
                        picture_url = item.pictureUrl?.value,
                        weight = weight,
                        notes = item.notes,
                )
            }

        }.andThen(findById(item.id).map { it.orNull()!! })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }
}
