package dev.ahmedmourad.sherlock.domain.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.domain.dagger.modules.AuthBindingsModule
import dev.ahmedmourad.sherlock.domain.dagger.modules.ChildrenBindingsModule
import dev.ahmedmourad.sherlock.domain.dagger.modules.CommonBindingsModule

@Module(includes = [
    ChildrenBindingsModule::class,
    AuthBindingsModule::class,
    CommonBindingsModule::class
])
interface DomainModule
