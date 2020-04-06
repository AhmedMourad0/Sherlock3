package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.services.SherlockService

@Subcomponent
internal interface SherlockServiceComponent {
    fun inject(service: SherlockService)
}
