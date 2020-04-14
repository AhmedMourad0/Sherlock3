package dev.ahmedmourad.sherlock.domain.filter

import arrow.core.toMap
import dev.ahmedmourad.sherlock.domain.filter.criteria.Criteria
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight

internal class ResultsFilter<T>(private val criteria: Criteria<T>) : Filter<T> {
    override fun filter(items: List<T>): Map<T, Weight> {
        return ArrayList(items).map { criteria.apply(it) }
                .filter { (_, score) -> score.passes() }
                .map { result -> result.map { it.calculate() } }
                .toMap()
    }
}
