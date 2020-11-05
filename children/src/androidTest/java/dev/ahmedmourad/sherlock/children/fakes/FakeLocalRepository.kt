package dev.ahmedmourad.sherlock.children.fakes

import arrow.core.*
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import io.reactivex.Single

internal class FakeLocalRepository : LocalRepository {

    private val fakeDb = mutableListOf<Pair<Either<SimpleRetrievedChild, RetrievedChild>, Weight?>>()

    var triggerInternalException = false
    var triggerUnknownException = false

    override fun insertOrReplaceRetainingWeight(
            item: RetrievedChild
    ): Flowable<Either<LocalRepository.UpdateRetainingWeightException, Tuple2<RetrievedChild, Weight?>>> {
        return Flowable.defer {

            when {

                triggerInternalException -> {
                    Flowable.just(LocalRepository.UpdateRetainingWeightException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Flowable.just(LocalRepository.UpdateRetainingWeightException.UnknownException(RuntimeException()).left())
                }

                else -> {

                    val existing = fakeDb.firstOrNull { pair ->
                        pair.first.fold(ifLeft = SimpleRetrievedChild::id, ifRight = RetrievedChild::id) == item.id
                    }

                    if (existing != null) {
                        fakeDb.removeIf { pair ->
                            pair.first.fold(ifLeft = SimpleRetrievedChild::id, ifRight = RetrievedChild::id) == item.id
                        }
                    }

                    fakeDb.add(item.right() to existing?.second)

                    Flowable.just((item toT existing?.second).right())
                }
            }
        }
    }

    override fun findAllSimpleWhereWeightExists():
            Flowable<Either<LocalRepository.FindAllSimpleWhereWeightExistsException, Map<SimpleRetrievedChild, Weight>>> {
        return Flowable.defer {

            when {

                triggerUnknownException -> {
                    Flowable.just(LocalRepository.FindAllSimpleWhereWeightExistsException.UnknownException(RuntimeException()).left())
                }

                else -> {

                    Flowable.just(fakeDb.mapNotNull { (child, weight) ->
                        weight?.let { w ->
                            child.fold(ifLeft = { it }, ifRight = RetrievedChild::simplify) to w
                        }
                    }.toMap().right())
                }
            }
        }
    }

    override fun replaceAll(
            items: Map<SimpleRetrievedChild, Weight>
    ): Single<Either<LocalRepository.ReplaceAllException, Map<SimpleRetrievedChild, Weight>>> {
        return Single.defer {

            when {

                triggerUnknownException -> {
                    Single.just(LocalRepository.ReplaceAllException.UnknownException(RuntimeException()).left())
                }

                else -> {

                    fakeDb.clear()
                    fakeDb.addAll(items.toList().map { it.first.left() to it.second })
                    Single.just(items.right())
                }
            }
        }
    }

    fun allResults(): MutableList<Pair<Either<SimpleRetrievedChild, RetrievedChild>, Weight?>> {
        return fakeDb
    }
}
