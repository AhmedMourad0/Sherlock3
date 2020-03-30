package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.ChildrenRemoteViewsFactoryModule
import inc.ahmedmourad.sherlock.dagger.modules.ChildrenRemoteViewsServiceModule
import inc.ahmedmourad.sherlock.widget.AppWidget
import inc.ahmedmourad.sherlock.widget.adapter.ChildrenRemoteViewsService

@Subcomponent(modules = [
    ChildrenRemoteViewsFactoryModule::class
])
internal interface ChildrenRemoteViewsServiceComponent {
    fun inject(service: ChildrenRemoteViewsService)
}

@Subcomponent(modules = [
    ChildrenRemoteViewsServiceModule::class
])
internal interface AppWidgetComponent {
    fun inject(widget: AppWidget)
}
