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
            impl: ObserveUserAuthStateInteractorImpl
    ): ObserveUserAuthStateInteractor

    @Binds
    fun bindObserveSignedInUserInteractor(
            impl: ObserveCurrentUserInteractorImpl
    ): ObserveCurrentUserInteractor

    @Binds
    fun bindSendPasswordResetEmailInteractor(
            impl: SendPasswordResetEmailInteractorImpl
    ): SendPasswordResetEmailInteractor

    @Binds
    fun bindSignInInteractor(
            impl: SignInInteractorImpl
    ): SignInInteractor

    @Binds
    fun bindSignInWithFacebookInteractor(
            impl: SignInWithFacebookInteractorImpl
    ): SignInWithFacebookInteractor

    @Binds
    fun bindSignInWithGoogleInteractor(
            impl: SignInWithGoogleInteractorImpl
    ): SignInWithGoogleInteractor

    @Binds
    fun bindSignInWithTwitterInteractor(
            impl: SignInWithTwitterInteractorImpl
    ): SignInWithTwitterInteractor

    @Binds
    fun bindSignOutInteractor(
            impl: SignOutInteractorImpl
    ): SignOutInteractor

    @Binds
    fun bindSignUpInteractor(
            impl: SignUpInteractorImpl
    ): SignUpInteractor

    @Binds
    fun bindCompleteSignUpInteractor(
            impl: CompleteSignUpInteractorImpl
    ): CompleteSignUpInteractor
}

@Module
internal interface ChildrenBindingsModule {

    @Binds
    fun bindAddChildInteractor(
            impl: AddChildInteractorImpl
    ): AddChildInteractor

    @Binds
    fun bindFindChildrenInteractor(
            impl: FindChildrenInteractorImpl
    ): FindChildrenInteractor

    @Binds
    fun bindFindChildInteractor(
            impl: FindChildInteractorImpl
    ): FindChildInteractor

    @Binds
    fun bindFindLastSearchResultsInteractor(
            impl: FindLastSearchResultsInteractorImpl
    ): FindLastSearchResultsInteractor

    @Binds
    @InternalApi
    fun bindChildrenCriteriaFactory(
            impl: ChildrenCriteriaFactoryImpl
    ): ChildrenCriteriaFactory

    @Binds
    fun bindChildrenFilterFactory(
            impl: ChildrenFilterFactoryImpl
    ): ChildrenFilterFactory
}

@Module
internal interface CommonBindingsModule {

    @Binds
    fun bindObserveInternetConnectivityInteractor(
            impl: ObserveInternetConnectivityInteractorImpl
    ): ObserveInternetConnectivityInteractor

    @Binds
    fun bindBus(
            impl: BusImpl
    ): Bus
}
