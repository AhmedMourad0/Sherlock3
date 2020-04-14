package dev.ahmedmourad.sherlock.android.dagger.components

import dagger.Subcomponent
import dev.ahmedmourad.sherlock.android.dagger.modules.ChildrenRemoteViewsFactoryModule
import dev.ahmedmourad.sherlock.android.dagger.modules.ChildrenRemoteViewsServiceModule
import dev.ahmedmourad.sherlock.android.widget.AppWidget
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsService

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
