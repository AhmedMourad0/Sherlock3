package dev.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import arrow.core.Tuple2
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

internal interface LocalRepository {

    fun updateIfExists(
            child: RetrievedChild
    ): Maybe<Either<UpdateIfExistsException, Tuple2<RetrievedChild, Weight?>>>

    fun findAllWithWeight():
            Flowable<Either<FindAllWithWeightException, Map<SimpleRetrievedChild, Weight>>>

    fun replaceAll(
            results: Map<RetrievedChild, Weight>
    ): Single<Either<ReplaceAllException, Map<SimpleRetrievedChild, Weight>>>

    fun clear(): Single<Either<ClearException, Unit>>

    sealed class UpdateIfExistsException {
        data class InternalException(val origin: Throwable) : UpdateIfExistsException()
        data class UnknownException(val origin: Throwable) : UpdateIfExistsException()
    }

    sealed class FindAllWithWeightException {
        data class InternalException(val origin: Throwable) : FindAllWithWeightException()
        data class UnknownException(val origin: Throwable) : FindAllWithWeightException()
    }

    sealed class ReplaceAllException {
        data class UnknownException(val origin: Throwable) : ReplaceAllException()
    }

    sealed class ClearException {
        data class UnknownException(val origin: Throwable) : ClearException()
    }
}
