package dev.ahmedmourad.sherlock.domain.filter.criteria

import arrow.core.Tuple2
import arrow.core.getOrHandle
import arrow.core.toT
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.LocationManager
import me.xdrop.fuzzywuzzy.FuzzySearch
import javax.inject.Inject

//TODO: date of losing and finding the child matters
internal class LooseCriteria(
        private val query: ChildQuery,
        private val locationManager: Lazy<LocationManager>
) : Criteria<RetrievedChild> {

    override fun apply(result: RetrievedChild): Tuple2<RetrievedChild, Criteria.Score> {
        return result toT Score(getFirstNameRatio(result),
                getLastNameRatio(result),
                getDistance(result),
                isSameGender(result),
                isSameHair(result),
                isSameSkin(result),
                getAgeError(result),
                getHeightError(result)
        )
    }

    private fun getFirstNameRatio(child: RetrievedChild): Int {
        return child.name?.fold(ifLeft = {
            FuzzySearch.ratio(query.fullName.first.value, it.value)
        }, ifRight = {
            FuzzySearch.ratio(query.fullName.first.value, it.first.value)
        }) ?: 50
    }

    private fun getLastNameRatio(child: RetrievedChild): Int {
        return child.name?.fold(ifLeft = {
            50
        }, ifRight = {
            FuzzySearch.ratio(query.fullName.last.value, it.last.value)
        }) ?: 50
    }

    private fun getDistance(child: RetrievedChild): Long {

        if (child.location == null) {
            return MAX_DISTANCE.value / 2
        }

        return locationManager.get().distanceBetween(query.location.coordinates.latitude,
                query.location.coordinates.longitude,
                child.location.coordinates.latitude,
                child.location.coordinates.longitude
        )
    }

    private fun isSameGender(child: RetrievedChild): Boolean {
        return query.appearance.gender == child.appearance.gender
    }

    private fun isSameSkin(child: RetrievedChild): Boolean {
        return query.appearance.skin == child.appearance.skin
    }

    private fun isSameHair(child: RetrievedChild): Boolean {
        return query.appearance.hair == child.appearance.hair
    }

    // the two-years padding is applied to account for user error when estimating age
    private fun getAgeError(child: RetrievedChild): Int {

        if (child.appearance.ageRange == null) {
            return 0
        }

        val min = child.appearance.ageRange.min.value - 2
        val max = child.appearance.ageRange.max.value + 2

        return when {
            query.appearance.age.value < min -> min - query.appearance.age.value
            query.appearance.age.value > max -> query.appearance.age.value - max
            else -> 0
        }
    }

    // the 15 cm padding is applied to account for user error when estimating height
    private fun getHeightError(child: RetrievedChild): Int {

        if (child.appearance.heightRange == null) {
            return 0
        }

        val min = child.appearance.heightRange.min.value - 15
        val max = child.appearance.heightRange.max.value + 15

        return when {
            query.appearance.height.value < min -> min - query.appearance.height.value
            query.appearance.height.value > max -> query.appearance.height.value - max
            else -> 0
        }
    }

    companion object {
        val MAX_DISTANCE = MaxDistance(5000L, 100L)
    }

    class Score(
            private val firstNameRatio: Int,
            private val lastNameRatio: Int,
            private val distance: Long,
            private val isSameGender: Boolean,
            private val isSameHair: Boolean,
            private val isSameSkin: Boolean,
            private val ageError: Int,
            private val heightError: Int
    ) : Criteria.Score {

        override fun passes() = true

        override fun calculate(): Weight {

            val score = getFirstNameScore() +
                    getLastNameScore() +
                    getDistanceScore() +
                    getGenderScore() +
                    getHairScore() +
                    getSkinScore() +
                    getAgeScore() +
                    getHeightScore()

            return Weight.of(score / MAX_SCORE).getOrHandle {
                throw ModelCreationException(it.toString())
            }
        }

        private fun getFirstNameScore() = firstNameRatio

        private fun getLastNameScore() = lastNameRatio

        private fun getDistanceScore(): Int {
            return if (distance <= MAX_DISTANCE.value) {
                100
            } else {
                (100 - ((distance - MAX_DISTANCE.value) / MAX_DISTANCE.factor))
                        .toInt()
                        .coerceAtLeast(0)
            }
        }

        private fun getGenderScore() = if (isSameGender) 100 else 0

        private fun getHairScore() = if (isSameHair) 50 else 0

        private fun getSkinScore() = if (isSameSkin) 50 else 0

        private fun getAgeScore() = (100 - (20 * ageError)).coerceAtLeast(0)

        private fun getHeightScore() = (100 - (5 * heightError)).coerceAtLeast(0)

        companion object {
            const val MAX_SCORE = 700.0
        }
    }

    class MaxDistance(val value: Long, val factor: Long)
}

interface ChildrenCriteriaFactory : (ChildQuery) -> Criteria<RetrievedChild>

@Reusable
internal class ChildrenCriteriaFactoryImpl @Inject constructor(
        private val locationManager: Lazy<LocationManager>
) : ChildrenCriteriaFactory {
    override fun invoke(query: ChildQuery): Criteria<RetrievedChild> {
        return LooseCriteria(query, locationManager)
    }
}
