package dev.ahmedmourad.sherlock.android.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.observe
import org.reactivestreams.Publisher

fun <T> Publisher<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

inline fun <T> LifecycleOwner.observe(liveData: LiveData<T>, crossinline observer: (T) -> Unit) {
    liveData.observe(this, observer)
}

//This prevents us from accidentally calling observe with the fragment itself as the lifecycle owner
inline fun <T> Fragment.observe(liveData: LiveData<T>, crossinline observer: (T) -> Unit) {
    viewLifecycleOwner.observe(liveData, observer)
}
