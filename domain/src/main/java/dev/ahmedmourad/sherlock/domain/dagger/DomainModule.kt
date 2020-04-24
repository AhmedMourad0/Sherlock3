package dev.ahmedmourad.sherlock.domain.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.domain.dagger.modules.AuthModuleBindings
import dev.ahmedmourad.sherlock.domain.dagger.modules.ChildrenModuleBindings
import dev.ahmedmourad.sherlock.domain.dagger.modules.CommonModuleBindings

@Module(includes = [
    ChildrenModuleBindings::class,
    AuthModuleBindings::class,
    CommonModuleBindings::class
])
object DomainModule
