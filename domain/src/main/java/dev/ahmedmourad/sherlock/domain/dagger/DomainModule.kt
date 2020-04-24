package dev.ahmedmourad.sherlock.domain.dagger

import dagger.Module

@Module(includes = [
    ChildrenBindingsModule::class,
    AuthBindingsModule::class,
    CommonBindingsModule::class
])
interface DomainModule
