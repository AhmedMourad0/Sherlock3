package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import io.reactivex.Single
import javax.inject.Inject

interface AddChildInteractor : (PublishedChild) -> Single<Either<Throwable, RetrievedChild>>

@Reusable
internal class AddChildInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : AddChildInteractor {
    override fun invoke(child: PublishedChild): Single<Either<Throwable, RetrievedChild>> {
        return childrenRepository.get().publish(child)
    }
}
