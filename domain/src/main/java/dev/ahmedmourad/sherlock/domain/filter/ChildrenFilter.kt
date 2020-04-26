package dev.ahmedmourad.sherlock.domain.filter

import arrow.core.toMap
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.di.InternalApi
import dev.ahmedmourad.sherlock.domain.filter.criteria.ChildrenCriteriaFactory
import dev.ahmedmourad.sherlock.domain.filter.criteria.Criteria
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import javax.inject.Inject

internal class ChildrenFilter<T>(private val criteria: Criteria<T>) : Filter<T> {
    override fun filter(items: List<T>): Map<T, Weight> {
        return ArrayList(items).map { criteria.apply(it) }
                .filter { (_, score) -> score.passes() }
                .map { result -> result.map { it.calculate() } }
                .toMap()
    }
}

interface ChildrenFilterFactory : (ChildQuery) -> Filter<RetrievedChild>

@Reusable
internal class ChildrenFilterFactoryImpl @Inject constructor(
        @InternalApi private val criteriaFactory: ChildrenCriteriaFactory
) : ChildrenFilterFactory {
    override fun invoke(query: ChildQuery): Filter<RetrievedChild> {
        return ChildrenFilter(criteriaFactory(query))
    }
}
