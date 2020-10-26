import {atLeast} from "./Utils";

export const MAX_SCORE = 700.0
export const MAX_DISTANCE = 150000
export const MAX_DISTANCE_FACTOR = MAX_DISTANCE / 30

export class ScoreCalculator {

    calculate(
        firstNameRatio: number,
        lastNameRatio: number,
        distance: number,
        isSameGender: boolean,
        isSameHair: boolean,
        isSameSkin: boolean,
        ageError: number,
        heightError: number
    ): number {
        const score = this.getFirstNameScore(firstNameRatio) +
            this.getLastNameScore(lastNameRatio) +
            this.getDistanceScore(distance) +
            this.getGenderScore(isSameGender) +
            this.getHairScore(isSameHair) +
            this.getSkinScore(isSameSkin) +
            this.getAgeScore(ageError) +
            this.getHeightScore(heightError)

        return score / MAX_SCORE
    }

    getFirstNameScore(firstNameRatio: number): number {
        return firstNameRatio
    }

    getLastNameScore(lastNameRatio: number): number {
        return lastNameRatio
    }

    getDistanceScore(distance: number): number {
        if (distance <= MAX_DISTANCE) {
            return 100
        } else {
            return atLeast(100 - ((distance - MAX_DISTANCE) / MAX_DISTANCE_FACTOR), 0)
        }
    }

    getGenderScore(isSameGender: boolean): number {
        return isSameGender ? 100 : 0
    }

    getHairScore(isSameHair: boolean): number {
        return isSameHair ? 50 : 0
    }

    getSkinScore(isSameSkin: boolean): number {
        return isSameSkin ? 50 : 0
    }

    getAgeScore(ageError: number): number {
        return atLeast(100 - (20 * ageError), 0)
    }

    getHeightScore(heightError: number): number {
        return atLeast(100 - (5 * heightError), 0)
    }
}
