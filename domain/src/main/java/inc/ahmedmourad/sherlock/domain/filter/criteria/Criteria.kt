package inc.ahmedmourad.sherlock.domain.filter.criteria

import arrow.core.Tuple2
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight

interface Criteria<T> {

    fun apply(result: T): Tuple2<T, Score>

    interface Score {
        fun passes(): Boolean
        fun calculate(): Weight
    }
}

