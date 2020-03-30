package inc.ahmedmourad.sherlock.idling

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import inc.ahmedmourad.sherlock.domain.bus.Event
import inc.ahmedmourad.sherlock.domain.constants.BackgroundState
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import timber.log.Timber
import timber.log.error

abstract class EventIdlingResource<T>(
        event: Event<T>,
        private val name: String) : IdlingResource {

    @Volatile
    private var resourceCallback: ResourceCallback? = null

    private var isIdle = true

    private var eventDisposable by disposable()

    init {
        eventDisposable = event.get().subscribe({
            isIdle = isIdleEvent(it)
        }, {
            Timber.error(it)
            isIdle = true
        })
    }


    abstract fun isIdleEvent(item: T): Boolean

    override fun getName(): String {
        return "${BackgroundEventIdlingResource::class.java.name} - $name"
    }

    override fun isIdleNow(): Boolean {
        return isIdle.also {
            if (it)
                resourceCallback?.onTransitionToIdle()
        }
    }

    override fun registerIdleTransitionCallback(resourceCallback: ResourceCallback) {
        this.resourceCallback = resourceCallback
    }

    fun dispose() {
        isIdle = true
        eventDisposable?.dispose()
    }
}

class BackgroundEventIdlingResource(event: Event<BackgroundState>, name: String) : EventIdlingResource<BackgroundState>(event, name) {
    override fun isIdleEvent(item: BackgroundState) = item != BackgroundState.ONGOING
}

class PublishingEventIdlingResource(event: Event<PublishingState>, name: String) : EventIdlingResource<PublishingState>(event, name) {
    override fun isIdleEvent(item: PublishingState) = item !is PublishingState.Ongoing
}

fun Event<BackgroundState>.toIdlingResource(name: String): BackgroundEventIdlingResource {
    return BackgroundEventIdlingResource(this, name)
}

fun Event<PublishingState>.toIdlingResource(name: String): PublishingEventIdlingResource {
    return PublishingEventIdlingResource(this, name)
}
