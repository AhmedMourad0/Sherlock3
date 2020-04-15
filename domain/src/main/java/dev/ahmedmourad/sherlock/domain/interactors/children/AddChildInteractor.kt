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

internal class AddChildInteractorImpl(
        private val childrenRepository: Lazy<ChildrenRepository>
) : AddChildInteractor {
    override fun invoke(child: PublishedChild): Single<Either<Throwable, RetrievedChild>> {
        return childrenRepository.get().publish(child)
    }
}
