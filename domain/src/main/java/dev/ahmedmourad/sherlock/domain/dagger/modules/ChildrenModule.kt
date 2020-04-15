package dev.ahmedmourad.sherlock.domain.dagger.modules

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenCriteriaFactory
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenCriteriaFactoryImpl
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactoryImpl
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.InternalApi
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.NotifyChildFindingStateChangeInteractorQualifier
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.NotifyChildrenFindingStateChangeInteractorQualifier
import dev.ahmedmourad.sherlock.domain.interactors.children.*
import dev.ahmedmourad.sherlock.domain.interactors.common.*

@Module
internal interface ChildrenModule {

    @Binds
    fun bindAddChildInteractor(
            interactor: AddChildInteractorImpl
    ): AddChildInteractor

    @Binds
    fun bindFindChildrenInteractor(
            interactor: FindChildrenInteractorImpl
    ): FindChildrenInteractor

    @Binds
    fun bindFindChildInteractor(
            interactor: FindChildInteractorImpl
    ): FindChildInteractor

    @Binds
    fun bindFindLastSearchResultsInteractor(
            interactor: FindLastSearchResultsInteractorImpl
    ): FindLastSearchResultsInteractor

    @Binds
    fun bindObserveChildPublishingStateInteractor(
            interactor: ObserveChildPublishingStateInteractorImpl
    ): ObserveChildPublishingStateInteractor

    @Binds
    fun bindCheckChildPublishingStateInteractor(
            interactor: CheckChildPublishingStateInteractorImpl
    ): CheckChildPublishingStateInteractor

    @Binds
    fun bindNotifyChildPublishingStateChangeInteractor(
            interactor: NotifyChildPublishingStateChangeInteractorImpl
    ): NotifyChildPublishingStateChangeInteractor

    @Binds
    @NotifyChildFindingStateChangeInteractorQualifier
    fun bindNotifyChildFindingStateChangeInteractor(
            interactor: NotifyChildFindingStateChangeInteractorImpl
    ): NotifyChildFindingStateChangeInteractor

    @Binds
    @NotifyChildrenFindingStateChangeInteractorQualifier
    fun bindNotifyChildrenFindingStateChangeInteractor(
            interactor: NotifyChildrenFindingStateChangeInteractorImpl
    ): NotifyChildrenFindingStateChangeInteractor

    @Binds
    @InternalApi
    fun bindChildrenCriteriaFactory(
            factory: ChildrenCriteriaFactoryImpl
    ): ChildrenCriteriaFactory

    @Binds
    fun bindChildrenFilterFactory(
            factory: ChildrenFilterFactoryImpl
    ): ChildrenFilterFactory
}
