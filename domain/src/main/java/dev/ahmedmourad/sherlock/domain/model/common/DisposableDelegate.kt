package dev.ahmedmourad.sherlock.domain.model.common

import io.reactivex.disposables.Disposable
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

class DisposableDelegate : ObservableProperty<Disposable?>(null) {
    override fun beforeChange(property: KProperty<*>, oldValue: Disposable?, newValue: Disposable?): Boolean {
        oldValue?.dispose()
        return super.beforeChange(property, oldValue, newValue)
    }
}

fun disposable() = DisposableDelegate()
