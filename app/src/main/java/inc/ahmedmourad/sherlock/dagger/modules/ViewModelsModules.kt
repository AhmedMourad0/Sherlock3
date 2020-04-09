package inc.ahmedmourad.sherlock.dagger.modules

import androidx.lifecycle.ViewModelProvider
import arrow.syntax.function.curried
import arrow.syntax.function.partially2
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.*
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import inc.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.*
import inc.ahmedmourad.sherlock.domain.interactors.auth.*
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.activity.MainActivityViewModel
import inc.ahmedmourad.sherlock.viewmodel.common.GlobalViewModel
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.*
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.*

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
    ): ViewModelProvider.NewInstanceFactory {
        return GlobalViewModel.Factory(
                observeInternetConnectivityInteractor,
                observeUserAuthStateInteractor,
                observeSignedInUserInteractor
        )
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
    ): ViewModelProvider.NewInstanceFactory {
        return MainActivityViewModel.Factory(signOutInteractor)
    }
}

@Module(includes = [
    SherlockServiceModule::class
])
internal object AddChildViewModelModule {
    @Provides
    @Reusable
    @AddChildViewModelQualifier
    @JvmStatic
    fun provide(
            @SherlockServiceIntentQualifier serviceFactory: SherlockServiceIntentFactory,
            observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return AddChildViewModel.Factory(
                serviceFactory,
                observeChildPublishingStateInteractor
        )
    }
}

@Module
internal object FindChildrenViewModelModule {
    @Provides
    @Reusable
    @FindChildrenViewModelQualifier
    @JvmStatic
    fun provide(): ViewModelProvider.NewInstanceFactory {
        return FindChildrenViewModel.Factory()
    }
}

@Module
internal object ResetPasswordViewModelModule {
    @Provides
    @Reusable
    @ResetPasswordViewModelQualifier
    @JvmStatic
    fun provide(
            sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return ResetPasswordViewModel.Factory(
                sendPasswordResetEmailInteractor
        )
    }
}

@Module
internal object SignedInUserProfileViewModelModule {
    @Provides
    @Reusable
    @SignedInUserProfileViewModelQualifier
    @JvmStatic
    fun provide(
            @ObserveSignedInUserInteractorQualifier observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return SignedInUserProfileViewModel.Factory(
                observeSignedInUserInteractor
        )
    }
}

@Module
internal object SignInViewModelModule {
    @Provides
    @Reusable
    @SignInViewModelQualifier
    @JvmStatic
    fun provide(
            signInInteractor: SignInInteractor,
            @SignInWithGoogleInteractorQualifier signInWithGoogleInteractor: SignInWithGoogleInteractor,
            @SignInWithFacebookInteractorQualifier signInWithFacebookInteractor: SignInWithFacebookInteractor,
            @SignInWithTwitterInteractorQualifier signInWithTwitterInteractor: SignInWithTwitterInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return SignInViewModel.Factory(
                signInInteractor,
                signInWithGoogleInteractor,
                signInWithFacebookInteractor,
                signInWithTwitterInteractor
        )
    }
}

@Module
internal object SignUpViewModelModule {
    @Provides
    @Reusable
    @SignUpViewModelQualifier
    @JvmStatic
    fun provide(
            signUpInteractor: SignUpInteractor,
            @SignInWithGoogleInteractorQualifier signUpWithGoogleInteractor: SignInWithGoogleInteractor,
            @SignInWithFacebookInteractorQualifier signUpWithFacebookInteractor: SignInWithFacebookInteractor,
            @SignInWithTwitterInteractorQualifier signUpWithTwitterInteractor: SignInWithTwitterInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return SignUpViewModel.Factory(
                signUpInteractor,
                signUpWithGoogleInteractor,
                signUpWithFacebookInteractor,
                signUpWithTwitterInteractor
        )
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
        return CompleteSignUpViewModel::Factory.partially2(completeSignUpInteractor)
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
        return ChildrenSearchResultsViewModel::Factory.curried()(interactor)(filterFactory)
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
        return ChildDetailsViewModel::Factory.partially2(interactor)
    }
}
