package dev.ahmedmourad.sherlock.domain.dagger.modules.factories

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.filter.ResultsFilter
import dev.ahmedmourad.sherlock.domain.filter.criteria.Criteria
import dev.ahmedmourad.sherlock.domain.filter.criteria.LooseCriteria
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.platform.LocationManager

typealias ChildrenFilterFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards Filter<RetrievedChild>

internal fun childrenFilterFactory(
        criteriaFactory: ChildrenCriteriaFactory,
        rules: ChildQuery
): Filter<RetrievedChild> {
    return ResultsFilter(criteriaFactory(rules))
}

typealias ChildrenCriteriaFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards Criteria<RetrievedChild>

internal fun childrenLooseCriteriaFactory(
        locationManager: Lazy<LocationManager>,
        rules: ChildQuery
): Criteria<RetrievedChild> {
    return LooseCriteria(rules, locationManager)
}
