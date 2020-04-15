package dev.ahmedmourad.sherlock.domain.dagger.modules.factories

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.InternalApi
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.filter.ResultsFilter
import dev.ahmedmourad.sherlock.domain.filter.criteria.Criteria
import dev.ahmedmourad.sherlock.domain.filter.criteria.LooseCriteria
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.platform.LocationManager
import javax.inject.Inject

typealias ChildrenFilterFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards Filter<RetrievedChild>

@Reusable
internal class ChildrenFilterFactoryImpl @Inject constructor(
        @InternalApi private val criteriaFactory: ChildrenCriteriaFactory
) : ChildrenFilterFactory {
    override fun invoke(query: ChildQuery): Filter<RetrievedChild> {
        return ResultsFilter(criteriaFactory(query))
    }
}

typealias ChildrenCriteriaFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards Criteria<RetrievedChild>

@Reusable
internal class ChildrenCriteriaFactoryImpl @Inject constructor(
        private val locationManager: Lazy<LocationManager>
) : ChildrenCriteriaFactory {
    override fun invoke(query: ChildQuery): Criteria<RetrievedChild> {
        return LooseCriteria(query, locationManager)
    }
}
