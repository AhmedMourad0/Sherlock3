package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.AddChildControllerModule
import inc.ahmedmourad.sherlock.dagger.modules.ChildDetailsControllerModule
import inc.ahmedmourad.sherlock.services.SherlockService

@Subcomponent(modules = [
    AddChildControllerModule::class,
    ChildDetailsControllerModule::class
])
internal interface SherlockServiceComponent {
    fun inject(service: SherlockService)
}
