package dev.ahmedmourad.sherlock.platform.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.platform.dagger.modules.ManagersModule

@Module(includes = [
    ManagersModule::class
])
object PlatformModule
