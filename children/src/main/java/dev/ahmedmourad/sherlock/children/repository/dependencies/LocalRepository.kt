package dev.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import arrow.core.Tuple2
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import io.reactivex.Single

internal interface LocalRepository {

    fun insertOrReplaceRetainingWeight(
            item: RetrievedChild
    ): Flowable<Either<UpdateRetainingWeightException, Tuple2<RetrievedChild, Weight?>>>

    fun findAllSimpleWhereWeightExists():
            Flowable<Either<FindAllSimpleWhereWeightExistsException, Map<SimpleRetrievedChild, Weight>>>

    fun replaceAll(
            items: Map<SimpleRetrievedChild, Weight>
    ): Single<Either<ReplaceAllException, Map<SimpleRetrievedChild, Weight>>>

    sealed class UpdateRetainingWeightException {
        data class InternalException(val origin: Throwable) : UpdateRetainingWeightException()
        data class UnknownException(val origin: Throwable) : UpdateRetainingWeightException()
    }

    sealed class FindAllSimpleWhereWeightExistsException {
        data class UnknownException(val origin: Throwable) : FindAllSimpleWhereWeightExistsException()
    }

    sealed class ReplaceAllException {
        data class UnknownException(val origin: Throwable) : ReplaceAllException()
    }
}
