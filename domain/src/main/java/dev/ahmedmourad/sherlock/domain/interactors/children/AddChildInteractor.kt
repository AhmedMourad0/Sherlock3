package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import io.reactivex.Single

typealias AddChildInteractor =
        (@JvmSuppressWildcards PublishedChild) ->
        @JvmSuppressWildcards Single<Either<Throwable, RetrievedChild>>

internal fun addChild(childrenRepository: Lazy<ChildrenRepository>, child: PublishedChild): Single<Either<Throwable, RetrievedChild>> {
    return childrenRepository.get().publish(child)
}
