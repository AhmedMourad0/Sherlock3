package dev.ahmedmourad.sherlock.domain.dagger.modules

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.domain.interactors.auth.*

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
