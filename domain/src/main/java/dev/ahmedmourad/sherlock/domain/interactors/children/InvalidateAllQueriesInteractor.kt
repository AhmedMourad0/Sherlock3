package dev.ahmedmourad.sherlock.domain.interactors.children

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import io.reactivex.Completable
import javax.inject.Inject

fun interface InvalidateAllQueriesInteractor : () -> Completable

@Reusable
internal class InvalidateAllQueriesInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : InvalidateAllQueriesInteractor {
    override fun invoke(): Completable {
        return childrenRepository.get().invalidateAllQueries()
    }
}
