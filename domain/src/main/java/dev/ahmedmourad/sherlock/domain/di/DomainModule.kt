package dev.ahmedmourad.sherlock.domain.di

import dagger.Module

@Module(includes = [
    ChildrenBindingsModule::class,
    AuthBindingsModule::class,
    CommonBindingsModule::class
])
interface DomainModule
