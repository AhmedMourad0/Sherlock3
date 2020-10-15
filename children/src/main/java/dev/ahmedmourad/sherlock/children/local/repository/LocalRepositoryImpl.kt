package dev.ahmedmourad.sherlock.children.local.repository

import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.left
import arrow.core.right
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.local.daos.ChildrenDao
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Reusable
internal class LocalRepositoryImpl @Inject constructor(
        @InternalApi private val childrenDao: ChildrenDao
) : LocalRepository {

    override fun insertOrReplaceRetainingWeight(
            item: RetrievedChild
    ): Flowable<Either<LocalRepository.UpdateRetainingWeightException, Tuple2<RetrievedChild, Weight?>>> {
        return childrenDao.insertOrReplaceRetainingWeight(item)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<Either<LocalRepository.UpdateRetainingWeightException, Tuple2<RetrievedChild, Weight?>>> { it.right() }
                .onErrorReturn {
                    LocalRepository.UpdateRetainingWeightException.UnknownException(it).left()
                }
    }

    override fun findAllSimpleWhereWeightExists():
            Flowable<Either<LocalRepository.FindAllSimpleWhereWeightExistsException, Map<SimpleRetrievedChild, Weight>>> {
        return childrenDao.findAllSimpleWhereWeightExists()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .distinctUntilChanged()
                .map<Either<LocalRepository.FindAllSimpleWhereWeightExistsException, Map<SimpleRetrievedChild, Weight>>> { it.right() }
                .onErrorReturn {
                    LocalRepository.FindAllSimpleWhereWeightExistsException.UnknownException(it).left()
                }
    }

    override fun replaceAll(
            items: Map<SimpleRetrievedChild, Weight>
    ): Single<Either<LocalRepository.ReplaceAllException, Map<SimpleRetrievedChild, Weight>>> {
        return childrenDao.replaceAll(items)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<Either<LocalRepository.ReplaceAllException, Map<SimpleRetrievedChild, Weight>>> { it.right() }
                .onErrorReturn {
                    LocalRepository.ReplaceAllException.UnknownException(it).left()
                }
    }
}
