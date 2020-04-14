package dev.ahmedmourad.sherlock.auth.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.auth.dagger.modules.AuthManagerModule

@Module(includes = [AuthManagerModule::class])
object AuthModule
