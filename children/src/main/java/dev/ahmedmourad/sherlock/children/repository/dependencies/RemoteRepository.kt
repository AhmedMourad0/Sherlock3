package dev.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
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

    fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<FindAllException, Map<SimpleRetrievedChild, Weight>>>

    fun clear(): Single<Either<ClearException, Unit>>

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

    sealed class FindAllException {
        object NoInternetConnectionException : FindAllException()
        object NoSignedInUserException : FindAllException()
        data class InternalException(val origin: Throwable) : FindAllException()
        data class UnknownException(val origin: Throwable) : FindAllException()
    }

    sealed class ClearException {
        object NoInternetConnectionException : ClearException()
        object NoSignedInUserException : ClearException()
        data class UnknownException(val origin: Throwable) : ClearException()
    }
}
