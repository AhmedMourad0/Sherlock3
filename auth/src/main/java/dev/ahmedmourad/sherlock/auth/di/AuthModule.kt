package dev.ahmedmourad.sherlock.auth.di

import dagger.Module

@Module(includes = [AuthBindingsModule::class, AuthProvidedModules::class])
interface AuthModule
