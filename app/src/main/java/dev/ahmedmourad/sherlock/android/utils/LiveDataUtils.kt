package dev.ahmedmourad.sherlock.android.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import org.reactivestreams.Publisher

fun <T : Any> Publisher<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, observer: Observer<in T>) {
    liveData.observe(this, observer)
}

fun <T> LifecycleOwner.observeAll(vararg liveData: LiveData<out T>, observer: Observer<in T>) {
    val mediatorLiveData = MediatorLiveData<T>()
    liveData.forEach { mediatorLiveData.addSource(it, mediatorLiveData::setValue) }
    mediatorLiveData.observe(this, observer)
}

//This prevents us from accidentally calling observe with the fragment itself as the lifecycle owner
fun <T> Fragment.observe(liveData: LiveData<T>, observer: Observer<in T>) {
    viewLifecycleOwner.observe(liveData, observer = observer)
}

//This prevents us from accidentally calling observe with the fragment itself as the lifecycle owner
fun <T> Fragment.observeAll(vararg liveData: LiveData<out T>, observer: Observer<in T>) {
    viewLifecycleOwner.observeAll(*liveData, observer = observer)
}
