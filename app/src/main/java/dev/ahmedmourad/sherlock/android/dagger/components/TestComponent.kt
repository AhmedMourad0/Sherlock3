package dev.ahmedmourad.sherlock.android.dagger.components

import dagger.Subcomponent
import dev.ahmedmourad.sherlock.android.dagger.modules.TextFormatterModule
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.platform.DateManager

@Subcomponent(modules = [
    TextFormatterModule::class
])
internal interface TestComponent {
    fun childrenRepository(): ChildrenRepository
    fun formatter(): Formatter
    fun dateManager(): DateManager
}
