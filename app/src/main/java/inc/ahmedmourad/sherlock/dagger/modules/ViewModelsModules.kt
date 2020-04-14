package inc.ahmedmourad.sherlock.dagger.modules

import arrow.syntax.function.*
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
import inc.ahmedmourad.sherlock.viewmodel.factory.SimpleViewModelFactoryFactory
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
    @ResetPasswordViewModelFactoryFactoryQualifier
    @JvmStatic
    fun provide(
            sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
    ): SimpleViewModelFactoryFactory {
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
