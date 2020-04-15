package dev.ahmedmourad.sherlock.domain.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.domain.dagger.modules.AuthModule
import dev.ahmedmourad.sherlock.domain.dagger.modules.ChildrenModule
import dev.ahmedmourad.sherlock.domain.dagger.modules.CommonModule

@Module(includes = [
    ChildrenModule::class,
    AuthModule::class,
    CommonModule::class
])
object DomainModule
