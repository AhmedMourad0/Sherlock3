package dev.ahmedmourad.sherlock.domain.data

import arrow.core.Either
import arrow.core.Tuple2
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface ChildrenRepository {

    fun publish(child: ChildToPublish): Single<Either<PublishException, RetrievedChild>>

    fun find(
            childId: ChildId
    ): Flowable<Either<FindException, Tuple2<RetrievedChild, Weight?>?>>

    fun findAll(
            query: ChildrenQuery
    ): Flowable<Either<FindAllException, Map<SimpleRetrievedChild, Weight>>>

    fun invalidateAllQueries(): Completable

    fun addInvestigation(
            investigation: Investigation
    ): Single<Either<AddInvestigationException, Investigation>>

    fun findAllInvestigations(): Flowable<Either<FindAllInvestigationsException, List<Investigation>>>

    fun findLastSearchResults(): Flowable<Either<FindLastSearchResultsException, Map<SimpleRetrievedChild, Weight>>>

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

    sealed class AddInvestigationException {
        object NoInternetConnectionException : AddInvestigationException()
        object NoSignedInUserException : AddInvestigationException()
        data class UnknownException(val origin: Throwable) : AddInvestigationException()
    }

    sealed class FindAllInvestigationsException {
        object NoInternetConnectionException : FindAllInvestigationsException()
        object NoSignedInUserException : FindAllInvestigationsException()
        data class UnknownException(val origin: Throwable) : FindAllInvestigationsException()
    }

    sealed class FindLastSearchResultsException {
        data class UnknownException(val origin: Throwable) : FindLastSearchResultsException()
    }
}
