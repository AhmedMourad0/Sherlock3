package inc.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable

typealias FindChildInteractor =
        (@JvmSuppressWildcards ChildId) ->
        @JvmSuppressWildcards Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>>

internal fun findChild(
        childrenRepository: Lazy<ChildrenRepository>,
        childId: ChildId
): Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>> {
    return childrenRepository.get().find(childId)
}
