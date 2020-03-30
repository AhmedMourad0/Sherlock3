package inc.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Single

internal interface ChildrenImageRepository {
    fun storeChildPicture(id: ChildId, picture: ByteArray?): Single<Either<Throwable, Url?>>
}
