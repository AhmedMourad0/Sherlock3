package dev.ahmedmourad.sherlock.children.local.repository

import arrow.core.*
import com.squareup.sqldelight.runtime.rx.asObservable
import com.squareup.sqldelight.runtime.rx.mapToList
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.local.ChildrenQueries
import dev.ahmedmourad.sherlock.children.local.UsersQueries
import dev.ahmedmourad.sherlock.children.local.utils.map
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Reusable
internal class LocalRepositoryImpl @Inject constructor(
        @InternalApi private val childrenQueries: ChildrenQueries,
        @InternalApi private val usersQueries: UsersQueries
) : LocalRepository {

    override fun replaceAll(
            items: Map<SimpleRetrievedChild, Weight>
    ): Single<Either<LocalRepository.ReplaceAllException, Map<SimpleRetrievedChild, Weight>>> {
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
                .map<Either<LocalRepository.ReplaceAllException, Map<SimpleRetrievedChild, Weight>>> { it.right() }
                .onErrorReturn {
                    LocalRepository.ReplaceAllException.UnknownException(it).left()
                }
    }

    override fun findAllSimpleWhereWeightExists():
            Flowable<Either<LocalRepository.FindAllSimpleWhereWeightExistsException, Map<SimpleRetrievedChild, Weight>>> {
        return childrenQueries.findAllSimpleWhereWeightExists()
                .asObservable()
                .mapToList()
                .map { l ->
                    l.mapNotNull { it.map().orNull() }.toMap()
                }.subscribeOn(Schedulers.io())
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged()
                .map<Either<LocalRepository.FindAllSimpleWhereWeightExistsException, Map<SimpleRetrievedChild, Weight>>> { it.right() }
                .onErrorReturn {
                    LocalRepository.FindAllSimpleWhereWeightExistsException.UnknownException(it).left()
                }
    }

    override fun insertOrReplaceRetainingWeight(
            item: RetrievedChild
    ): Flowable<Either<LocalRepository.InsertOrReplaceRetainingWeightException, Tuple2<RetrievedChild, Weight?>>> {
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
                .map<Either<LocalRepository.InsertOrReplaceRetainingWeightException, Tuple2<RetrievedChild, Weight?>>> { it.right() }
                .onErrorReturn {
                    LocalRepository.InsertOrReplaceRetainingWeightException.UnknownException(it).left()
                }
    }

    private fun findById(id: ChildId): Flowable<Option<Tuple2<RetrievedChild, Weight?>>> {
        return childrenQueries.findChildById(id.value)
                .asObservable()
                .mapToList()
                .map { l ->
                    l.firstOrNull()?.map(id)?.orNull().toOption()
                }.subscribeOn(Schedulers.io())
                .toFlowable(BackpressureStrategy.LATEST)
    }
}
