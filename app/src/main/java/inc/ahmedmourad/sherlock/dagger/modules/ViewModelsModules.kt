package inc.ahmedmourad.sherlock.dagger.modules

import androidx.lifecycle.ViewModelProvider
import arrow.syntax.function.curried
import arrow.syntax.function.partially1
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
import inc.ahmedmourad.sherlock.domain.interactors.common.CheckChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.CheckInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.activity.factory.MainActivityViewModelFactory
import inc.ahmedmourad.sherlock.viewmodel.common.factories.GlobalViewModelFactory
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.factories.*
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories.*

@Module
internal object GlobalViewModelModule {
    @Provides
    @Reusable
    @GlobalViewModelQualifier
    @JvmStatic
    fun provideMainActivityViewModel(
            @ObserveInternetConnectivityInteractorQualifier observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
            @ObserveUserAuthStateInteractorQualifier observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
            @ObserveSignedInUserInteractorQualifier observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return GlobalViewModelFactory(
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
    fun provideMainActivityViewModel(
            signOutInteractor: SignOutInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return MainActivityViewModelFactory(signOutInteractor)
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
    fun provideAddChildViewModel(
            @SherlockServiceIntentQualifier serviceFactory: SherlockServiceIntentFactory,
            @ObserveInternetConnectivityInteractorQualifier observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
            @CheckInternetConnectivityInteractorQualifier checkInternetConnectivityInteractor: CheckInternetConnectivityInteractor,
            observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor,
            checkChildPublishingStateInteractor: CheckChildPublishingStateInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return AddChildViewModelFactory(
                serviceFactory,
                observeInternetConnectivityInteractor,
                checkInternetConnectivityInteractor,
                observeChildPublishingStateInteractor,
                checkChildPublishingStateInteractor
        )
    }
}

@Module
internal object FindChildrenViewModelModule {
    @Provides
    @Reusable
    @FindChildrenViewModelQualifier
    @JvmStatic
    fun provideFindChildrenViewModel(
            @ObserveInternetConnectivityInteractorQualifier observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return FindChildrenViewModelFactory(
                observeInternetConnectivityInteractor
        )
    }
}

@Module
internal object ResetPasswordViewModelModule {
    @Provides
    @Reusable
    @ResetPasswordViewModelQualifier
    @JvmStatic
    fun provideResetPasswordViewModel(
            sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return ResetPasswordViewModelFactory(
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
    fun provideSignedInUserProfileViewModel(
            @ObserveSignedInUserInteractorQualifier observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return SignedInUserProfileViewModelFactory(
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
    fun provideSignInViewModel(
            signInInteractor: SignInInteractor,
            @SignInWithGoogleInteractorQualifier signInWithGoogleInteractor: SignInWithGoogleInteractor,
            @SignInWithFacebookInteractorQualifier signInWithFacebookInteractor: SignInWithFacebookInteractor,
            @SignInWithTwitterInteractorQualifier signInWithTwitterInteractor: SignInWithTwitterInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return SignInViewModelFactory(
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
    fun provideSignUpViewModel(
            signUpInteractor: SignUpInteractor,
            @SignInWithGoogleInteractorQualifier signUpWithGoogleInteractor: SignInWithGoogleInteractor,
            @SignInWithFacebookInteractorQualifier signUpWithFacebookInteractor: SignInWithFacebookInteractor,
            @SignInWithTwitterInteractorQualifier signUpWithTwitterInteractor: SignInWithTwitterInteractor
    ): ViewModelProvider.NewInstanceFactory {
        return SignUpViewModelFactory(
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
    fun provideCompleteSignUpViewModel(
            completeSignUpInteractor: CompleteSignUpInteractor
    ): CompleteSignUpViewModelFactoryFactory {
        return ::completeSignUpViewModelFactoryFactory.partially1(completeSignUpInteractor)
    }
}

@Module
internal object ChildrenSearchResultsViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideChildrenSearchResultViewModel(
            interactor: FindChildrenInteractor,
            filterFactory: ChildrenFilterFactory
    ): ChildrenSearchResultsViewModelFactoryFactory {
        return ::childrenSearchResultsViewModelFactoryFactory.curried()(interactor)(filterFactory)
    }
}

@Module
internal object ChildDetailsViewModelModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideChildDetailsViewModel(
            interactor: FindChildInteractor
    ): ChildDetailsViewModelFactoryFactory {
        return ::childDetailsViewModelFactoryFactory.partially1(interactor)
    }
}
