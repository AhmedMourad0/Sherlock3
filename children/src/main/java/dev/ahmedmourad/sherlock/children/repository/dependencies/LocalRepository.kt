package dev.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import arrow.core.Tuple2
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

internal interface LocalRepository {

    fun updateIfExists(
            child: RetrievedChild
    ): Maybe<Either<Throwable, Tuple2<RetrievedChild, Weight?>>>

    fun findAllWithWeight(): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

    fun replaceAll(
            results: Map<RetrievedChild, Weight>
    ): Single<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

    fun clear(): Completable
}
