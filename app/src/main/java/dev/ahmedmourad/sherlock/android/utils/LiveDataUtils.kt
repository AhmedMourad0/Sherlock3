package dev.ahmedmourad.sherlock.android.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import org.reactivestreams.Publisher

fun <T> Publisher<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

inline fun <T> LifecycleOwner.observe(liveData: LiveData<T>, crossinline observer: (T) -> Unit) {
    liveData.observe(this, observer)
}

inline fun <T> LifecycleOwner.observeAll(vararg liveData: LiveData<out T>, crossinline observer: (T) -> Unit) {
    val mediatorLiveData = MediatorLiveData<T>()
    liveData.forEach { mediatorLiveData.addSource(it, mediatorLiveData::setValue) }
    mediatorLiveData.observe(this, observer)
}

//This prevents us from accidentally calling observe with the fragment itself as the lifecycle owner
inline fun <T> Fragment.observe(liveData: LiveData<T>, crossinline observer: (T) -> Unit) {
    viewLifecycleOwner.observe(liveData, observer = observer)
}

//This prevents us from accidentally calling observe with the fragment itself as the lifecycle owner
inline fun <T> Fragment.observeAll(vararg liveData: LiveData<out T>, crossinline observer: (T) -> Unit) {
    viewLifecycleOwner.observeAll(*liveData, observer = observer)
}
