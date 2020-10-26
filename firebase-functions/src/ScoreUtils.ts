import * as stringSimilarity from "string-similarity";
import {ScoreCalculator, MAX_DISTANCE} from "./ScoreCalculator";
import {distanceBetweenInMeters, isSafe} from "./Utils"

export const calculator = new ScoreCalculator()

export function calculateWeight(
    findChildProperty: (key: string) => any,
    findInvestigationOrQueryProperty: (key: string) => any
): number {

    const childLat = findChildProperty("location_latitude")
    const childLong = findChildProperty("location_longitude")

    const childCoords = (isSafe(childLat) && isSafe(childLong)) ? {
        lat: childLat,
        long: childLong
    } : null

    const investigationCoords = {
        lat: findInvestigationOrQueryProperty("latitude"),
        long: findInvestigationOrQueryProperty("longitude")
    }

    const childMinAge = findChildProperty("min_age")
    const childMaxAge = findChildProperty("max_age")

    const childAgeRange = (isSafe(childMinAge) && isSafe(childMaxAge)) ? {
        min: childMinAge,
        max: childMaxAge
    } : null

    const childMinHeight = findChildProperty("min_height")
    const childMaxHeight = findChildProperty("max_height")

    const childHeightRange = (isSafe(childMinHeight) && isSafe(childMaxHeight)) ? {
        min: childMinHeight,
        max: childMaxHeight
    } : null

    return calculator.calculate(
        getFirstNameRatio(findChildProperty("first_name"), findInvestigationOrQueryProperty("first_name")),
        getLastNameRatio(findChildProperty("last_name"), findInvestigationOrQueryProperty("last_name")),
        getDistance(childCoords, investigationCoords),
        isSameGender(findChildProperty("gender"), findInvestigationOrQueryProperty("gender")),
        isSameHair(findChildProperty("hair"), findInvestigationOrQueryProperty("hair")),
        isSameSkin(findChildProperty("skin"), findInvestigationOrQueryProperty("skin")),
        getAgeError(childAgeRange, findInvestigationOrQueryProperty("age")),
        getHeightError(childHeightRange, findInvestigationOrQueryProperty("height"))
    )
}

function getFirstNameRatio(childFirstName: string, investigationFirstName: string): number {
    return isSafe(childFirstName) ? stringSimilarity.compareTwoStrings(investigationFirstName, childFirstName) : 50
}

function getLastNameRatio(childLastName: string, investigationLastName: string): number {
    return isSafe(childLastName) ? stringSimilarity.compareTwoStrings(investigationLastName, childLastName) : 50
}

function getDistance(
    childCoords: { lat: number, long: number } | null,
    investigationCoords: { lat: number, long: number }
): number {

    if (!isSafe(childCoords)) {
        return MAX_DISTANCE / 2
    }

    return distanceBetweenInMeters(
        investigationCoords.lat,
        investigationCoords.long,
        childCoords!.lat,
        childCoords!.long
    )
}

function isSameGender(childGender: number, investigationGender: number): boolean {
    return investigationGender === childGender
}

function isSameSkin(childSkin: number, investigationSkin: number): boolean {
    return investigationSkin === childSkin
}

function isSameHair(childHair: number, investigationHair: number): boolean {
    return investigationHair === childHair
}

// the two-years padding is applied to account for user error when estimating age
function getAgeError(
    childAgeRange: { min: number, max: number } | null,
    investigationAge: number
): number {

    if (!isSafe(childAgeRange)) {
        return 0
    }

    const min = childAgeRange!.min - 2
    const max = childAgeRange!.max + 2

    if (investigationAge < min) {
        return min - investigationAge
    } else if (investigationAge > max) {
        return investigationAge - max
    } else {
        return 0
    }
}

// the 15 cm padding is applied to account for user error when estimating height
function getHeightError(
    childHeightRange: { min: number, max: number } | null,
    investigationHeight: number
): number {

    if (!isSafe(childHeightRange)) {
        return 0
    }

    const min = childHeightRange!.min - 15
    const max = childHeightRange!.max + 15

    if (investigationHeight < min) {
        return min - investigationHeight
    } else if (investigationHeight > max) {
        return investigationHeight - max
    } else {
        return 0
    }
}
