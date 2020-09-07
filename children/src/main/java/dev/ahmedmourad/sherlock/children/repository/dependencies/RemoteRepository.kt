package dev.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

internal interface RemoteRepository {

    fun publish(
            childId: ChildId,
            child: ChildToPublish,
            pictureUrl: Url?
    ): Single<Either<PublishException, RetrievedChild>>

    fun find(
            childId: ChildId
    ): Flowable<Either<FindException, RetrievedChild?>>

    fun addInvestigation(
            investigation: Investigation
    ): Single<Either<AddInvestigationException, Investigation>>

    fun findAllInvestigations(): Flowable<Either<FindAllInvestigationsException, List<Investigation>>>

    fun findAll(
            query: ChildrenQuery
    ): Flowable<Either<FindAllException, Map<SimpleRetrievedChild, Weight>>>

    fun invalidateAllQueries(): Completable

    sealed class PublishException {
        object NoInternetConnectionException : PublishException()
        object NoSignedInUserException : PublishException()
        data class UnknownException(val origin: Throwable) : PublishException()
    }

    sealed class FindException {
        object NoInternetConnectionException : FindException()
        object NoSignedInUserException : FindException()
        data class InternalException(val origin: Throwable) : FindException()
        data class UnknownException(val origin: Throwable) : FindException()
    }

    sealed class AddInvestigationException {
        object NoInternetConnectionException : AddInvestigationException()
        object NoSignedInUserException : AddInvestigationException()
        data class UnknownException(val origin: Throwable) : AddInvestigationException()
    }

    sealed class FindAllInvestigationsException {
        object NoInternetConnectionException : FindAllInvestigationsException()
        object NoSignedInUserException : FindAllInvestigationsException()
        data class InternalException(val origin: Throwable) : FindAllInvestigationsException()
        data class UnknownException(val origin: Throwable) : FindAllInvestigationsException()
    }

    sealed class FindAllException {
        object NoInternetConnectionException : FindAllException()
        object NoSignedInUserException : FindAllException()
        data class InternalException(val origin: Throwable) : FindAllException()
        data class UnknownException(val origin: Throwable) : FindAllException()
    }
}
