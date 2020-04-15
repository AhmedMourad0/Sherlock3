package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import javax.inject.Inject

typealias FindChildrenInteractor =
        (@JvmSuppressWildcards ChildQuery, @JvmSuppressWildcards Filter<RetrievedChild>) ->
        @JvmSuppressWildcards Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

@Reusable
internal class FindChildrenInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindChildrenInteractor {
    override fun invoke(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
        return childrenRepository.get().findAll(query, filter)
    }
}
