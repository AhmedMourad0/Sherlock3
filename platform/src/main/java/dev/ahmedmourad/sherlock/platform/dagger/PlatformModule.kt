package dev.ahmedmourad.sherlock.platform.dagger

import dagger.Module

@Module(includes = [
    PlatformBindingsModule::class
])
interface PlatformModule
