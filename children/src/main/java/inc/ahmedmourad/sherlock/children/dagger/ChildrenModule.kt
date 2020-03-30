package inc.ahmedmourad.sherlock.children.dagger

import dagger.Module
import inc.ahmedmourad.sherlock.children.dagger.modules.ChildrenRepositoryModule

@Module(includes = [ChildrenRepositoryModule::class])
object ChildrenModule
