package dev.ahmedmourad.sherlock.domain.data

import arrow.core.Either
import arrow.core.Tuple2
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import io.reactivex.Single

interface ChildrenRepository {

    fun publish(child: PublishedChild): Single<Either<PublishException, RetrievedChild>>

    fun find(
            childId: ChildId
    ): Flowable<Either<FindException, Tuple2<RetrievedChild, Weight?>?>>

    fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<FindAllException, Map<SimpleRetrievedChild, Weight>>>

    fun findLastSearchResults(): Flowable<Either<FindLastSearchResultsException, Map<SimpleRetrievedChild, Weight>>>

    fun test(): Tester

    interface Tester {

        fun clear(): Single<Either<ClearException, Unit>>

        sealed class ClearException {
            object NoInternetConnectionException : ClearException()
            object NoSignedInUserException : ClearException()
            data class UnknownException(val origin: Throwable) : ClearException()
        }
    }

    sealed class PublishException {
        object NoInternetConnectionException : PublishException()
        object NoSignedInUserException : PublishException()
        data class InternalException(val origin: Throwable) : PublishException()
        data class UnknownException(val origin: Throwable) : PublishException()
    }

    sealed class FindException {
        object NoInternetConnectionException : FindException()
        object NoSignedInUserException : FindException()
        data class InternalException(val origin: Throwable) : FindException()
        data class UnknownException(val origin: Throwable) : FindException()
    }

    sealed class FindAllException {
        object NoInternetConnectionException : FindAllException()
        object NoSignedInUserException : FindAllException()
        data class UnknownException(val origin: Throwable) : FindAllException()
    }

    sealed class FindLastSearchResultsException {
        data class InternalException(val origin: Throwable) : FindLastSearchResultsException()
        data class UnknownException(val origin: Throwable) : FindLastSearchResultsException()
    }
}
