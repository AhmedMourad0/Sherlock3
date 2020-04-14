package dev.ahmedmourad.sherlock.android.dagger.modules

import arrow.syntax.function.*
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.SherlockServiceIntentFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.qualifiers.*
import dev.ahmedmourad.sherlock.android.viewmodel.activity.MainActivityViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.common.GlobalViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleViewModelFactoryFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.*
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.*
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.*
import dev.ahmedmourad.sherlock.domain.interactors.auth.*
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor

@Module
internal object GlobalViewModelModule {
    @Provides
    @Reusable
    @GlobalViewModelQualifier
    @JvmStatic
    fun provide(
            @ObserveInternetConnectivityInteractorQualifier observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
            @ObserveUserAuthStateInteractorQualifier observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
            @ObserveSignedInUserInteractorQualifier observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ): SimpleViewModelFactoryFactory {
        return GlobalViewModel::Factory.reverse()
                .curried()
                .invoke(observeSignedInUserInteractor)
                .invoke(observeUserAuthStateInteractor)
                .invoke(observeInternetConnectivityInteractor)
    }
}

@Module
internal object MainActivityViewModelModule {
    @Provides
    @Reusable
    @MainActivityViewModelQualifier
    @JvmStatic
    fun provide(
            signOutInteractor: SignOutInteractor
    ): SimpleViewModelFactoryFactory {
        return MainActivityViewModel::Factory.partially2(signOutInteractor)
    }
}

@Module(includes = [
    SherlockServiceModule::class
])
internal object AddChildViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            @SherlockServiceIntentQualifier serviceFactory: SherlockServiceIntentFactory,
            observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor
    ): AddChildViewModelFactoryFactory {
        return AddChildViewModel::Factory.partially3(serviceFactory)
                .partially3(observeChildPublishingStateInteractor)
    }
}

@Module
internal object FindChildrenViewModelModule {
    @Provides
    @Reusable
    @FindChildrenViewModelFactoryFactoryQualifier
    @JvmStatic
    fun provide(): SimpleViewModelFactoryFactory {
        return FindChildrenViewModel::Factory
    }
}

@Module
internal object ResetPasswordViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
    ): ResetPasswordViewModelFactoryFactory {
        return ResetPasswordViewModel::Factory.partially1(
                sendPasswordResetEmailInteractor
        )
    }
}

@Module
internal object SignedInUserProfileViewModelModule {
    @Provides
    @Reusable
    @SignedInUserProfileViewModelFactoryFactoryQualifier
    @JvmStatic
    fun provide(
            @ObserveSignedInUserInteractorQualifier observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ): SimpleViewModelFactoryFactory {
        return SignedInUserProfileViewModel::Factory.partially2(
                observeSignedInUserInteractor
        )
    }
}

@Module
internal object SignInViewModelModule {
    @Provides
    @Reusable
    @SignInViewModelFactoryFactoryQualifier
    @JvmStatic
    fun provide(
            signInInteractor: SignInInteractor,
            @SignInWithGoogleInteractorQualifier signInWithGoogleInteractor: SignInWithGoogleInteractor,
            @SignInWithFacebookInteractorQualifier signInWithFacebookInteractor: SignInWithFacebookInteractor,
            @SignInWithTwitterInteractorQualifier signInWithTwitterInteractor: SignInWithTwitterInteractor
    ): SimpleViewModelFactoryFactory {
        return SignInViewModel::Factory.reverse()
                .curried()
                .invoke(signInWithTwitterInteractor)
                .invoke(signInWithFacebookInteractor)
                .invoke(signInWithGoogleInteractor)
                .invoke(signInInteractor)
    }
}

@Module
internal object SignUpViewModelModule {
    @Provides
    @Reusable
    @SignUpViewModelFactoryFactoryQualifier
    @JvmStatic
    fun provide(
            signUpInteractor: SignUpInteractor,
            @SignInWithGoogleInteractorQualifier signUpWithGoogleInteractor: SignInWithGoogleInteractor,
            @SignInWithFacebookInteractorQualifier signUpWithFacebookInteractor: SignInWithFacebookInteractor,
            @SignInWithTwitterInteractorQualifier signUpWithTwitterInteractor: SignInWithTwitterInteractor
    ): SimpleViewModelFactoryFactory {
        return SignUpViewModel::Factory.reverse()
                .curried()
                .invoke(signUpWithTwitterInteractor)
                .invoke(signUpWithFacebookInteractor)
                .invoke(signUpWithGoogleInteractor)
                .invoke(signUpInteractor)
    }
}

@Module
internal object CompleteSignUpViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            completeSignUpInteractor: CompleteSignUpInteractor
    ): CompleteSignUpViewModelFactoryFactory {
        return CompleteSignUpViewModel::Factory.partially3(completeSignUpInteractor)
    }
}

@Module
internal object ChildrenSearchResultsViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            interactor: FindChildrenInteractor,
            filterFactory: ChildrenFilterFactory
    ): ChildrenSearchResultsViewModelFactoryFactory {
        return ChildrenSearchResultsViewModel::Factory.partially3(filterFactory)
                .partially2(interactor)
    }
}

@Module
internal object ChildDetailsViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            interactor: FindChildInteractor
    ): ChildDetailsViewModelFactoryFactory {
        return ChildDetailsViewModel::Factory.partially3(interactor)
    }
}
