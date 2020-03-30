package inc.ahmedmourad.sherlock.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.archlifecycle.LifecycleController

internal fun Controller.setSupportActionBar(toolbar: Toolbar) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
}

internal fun LifecycleController.viewModelProvider(factory: ViewModelProvider.NewInstanceFactory?): ViewModelProvider {
    return if (factory == null) viewModelProvider() else ViewModelProvider(ViewModelStore(), factory)
}

internal fun LifecycleController.viewModelProvider(): ViewModelProvider {
    return ViewModelProvider(ViewModelStore(), defaultFactory())
}

internal fun LifecycleController.defaultFactory(): ViewModelProvider.AndroidViewModelFactory {
    return ViewModelProvider.AndroidViewModelFactory(
            checkNotNull(activity?.application)
    )
}
