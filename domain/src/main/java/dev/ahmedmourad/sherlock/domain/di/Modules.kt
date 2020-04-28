package dev.ahmedmourad.sherlock.domain.di

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.bus.BusImpl
import dev.ahmedmourad.sherlock.domain.filter.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.filter.ChildrenFilterFactoryImpl
import dev.ahmedmourad.sherlock.domain.filter.criteria.ChildrenCriteriaFactory
import dev.ahmedmourad.sherlock.domain.filter.criteria.ChildrenCriteriaFactoryImpl
import dev.ahmedmourad.sherlock.domain.interactors.auth.*
import dev.ahmedmourad.sherlock.domain.interactors.children.*
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractorImpl

@Module
internal interface AuthBindingsModule {

    @Binds
    fun bindObserveUserAuthStateInteractor(
            interactor: ObserveUserAuthStateInteractorImpl
    ): ObserveUserAuthStateInteractor

    @Binds
    fun bindObserveSignedInUserInteractor(
            interactor: ObserveSignedInUserInteractorImpl
    ): ObserveSignedInUserInteractor

    @Binds
    fun bindSendPasswordResetEmailInteractor(
            interactor: SendPasswordResetEmailInteractorImpl
    ): SendPasswordResetEmailInteractor

    @Binds
    fun bindSignInInteractor(
            interactor: SignInInteractorImpl
    ): SignInInteractor

    @Binds
    fun bindSignInWithFacebookInteractor(
            interactor: SignInWithFacebookInteractorImpl
    ): SignInWithFacebookInteractor

    @Binds
    fun bindSignInWithGoogleInteractor(
            interactor: SignInWithGoogleInteractorImpl
    ): SignInWithGoogleInteractor

    @Binds
    fun bindSignInWithTwitterInteractor(
            interactor: SignInWithTwitterInteractorImpl
    ): SignInWithTwitterInteractor

    @Binds
    fun bindSignOutInteractor(
            interactor: SignOutInteractorImpl
    ): SignOutInteractor

    @Binds
    fun bindSignUpInteractor(
            interactor: SignUpInteractorImpl
    ): SignUpInteractor

    @Binds
    fun bindCompleteSignUpInteractor(
            interactor: CompleteSignUpInteractorImpl
    ): CompleteSignUpInteractor
}

@Module
internal interface ChildrenBindingsModule {

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
    @InternalApi
    fun bindChildrenCriteriaFactory(
            factory: ChildrenCriteriaFactoryImpl
    ): ChildrenCriteriaFactory

    @Binds
    fun bindChildrenFilterFactory(
            factory: ChildrenFilterFactoryImpl
    ): ChildrenFilterFactory
}

@Module
internal interface CommonBindingsModule {

    @Binds
    fun bindObserveInternetConnectivityInteractor(
            interactor: ObserveInternetConnectivityInteractorImpl
    ): ObserveInternetConnectivityInteractor

    @Binds
    fun bindBus(
            bus: BusImpl
    ): Bus
}
