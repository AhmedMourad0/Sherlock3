package inc.ahmedmourad.sherlock.domain.dagger.modules.factories

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.filter.Filter
import inc.ahmedmourad.sherlock.domain.filter.ResultsFilter
import inc.ahmedmourad.sherlock.domain.filter.criteria.Criteria
import inc.ahmedmourad.sherlock.domain.filter.criteria.LooseCriteria
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.platform.LocationManager

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
