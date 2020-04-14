package dev.ahmedmourad.sherlock.android.dagger.components

import dagger.Subcomponent
import dev.ahmedmourad.sherlock.android.services.SherlockService

@Subcomponent
internal interface SherlockServiceComponent {
    fun inject(service: SherlockService)
}
