package dev.ahmedmourad.sherlock.children.di

import dagger.Module

@Module(includes = [ChildrenBindingsModule::class, ChildrenProvidedModule::class])
interface ChildrenModule
