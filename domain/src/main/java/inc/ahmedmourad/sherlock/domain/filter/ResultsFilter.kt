package inc.ahmedmourad.sherlock.domain.filter

import arrow.core.Tuple2
import inc.ahmedmourad.sherlock.domain.filter.criteria.Criteria
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight

internal class ResultsFilter<T>(private val criteria: Criteria<T>) : Filter<T> {
    override fun filter(items: List<T>): List<Tuple2<T, Weight>> {
        return ArrayList(items).map { criteria.apply(it) }
                .filter { (_, score) -> score.passes() }
                .map { result -> result.map { it.calculate() } }
                .sortedByDescending { it.b.value }
    }
}
