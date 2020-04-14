package dev.ahmedmourad.sherlock.children.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.children.dagger.modules.ChildrenRepositoryModule

@Module(includes = [ChildrenRepositoryModule::class])
object ChildrenModule
