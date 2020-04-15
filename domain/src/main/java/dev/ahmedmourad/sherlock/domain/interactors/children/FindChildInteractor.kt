package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import arrow.core.Tuple2
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import javax.inject.Inject

typealias FindChildInteractor =
        (@JvmSuppressWildcards ChildId) ->
        @JvmSuppressWildcards Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>>

@Reusable
internal class FindChildInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindChildInteractor {
    override fun invoke(childId: ChildId): Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>> {
        return childrenRepository.get().find(childId)
    }
}
