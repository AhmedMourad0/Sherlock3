package dev.ahmedmourad.sherlock.android.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.observe
import org.reactivestreams.Publisher

fun <T> Publisher<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

inline fun <T> LifecycleOwner.observe(liveData: LiveData<T>, crossinline observer: (T) -> Unit) {
    liveData.observe(this, observer)
}
