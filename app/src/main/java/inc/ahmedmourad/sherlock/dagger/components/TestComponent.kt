package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.TextFormatterModule
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter

@Subcomponent(modules = [
    TextFormatterModule::class
])
internal interface TestComponent {
    fun childrenRepository(): ChildrenRepository
    fun formatter(): Formatter
    fun dateManager(): DateManager
}
