package dev.ahmedmourad.sherlock.domain.filter

import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight

//TODO: should be a function
interface Filter<T> {
    //TODO: return a double as a ratio that this is the right child
    fun filter(items: List<T>): Map<T, Weight>
}
