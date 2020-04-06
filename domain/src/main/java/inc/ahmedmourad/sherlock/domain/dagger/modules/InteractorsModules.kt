package inc.ahmedmourad.sherlock.domain.dagger.modules

import arrow.syntax.function.partially1
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.domain.bus.Bus
import inc.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.*
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.interactors.auth.*
import inc.ahmedmourad.sherlock.domain.interactors.children.*
import inc.ahmedmourad.sherlock.domain.interactors.common.*
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager

@Module
internal object AddChildInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideAddChildInteractor(childrenRepository: Lazy<ChildrenRepository>): AddChildInteractor {
        return ::addChild.partially1(childrenRepository)
    }
}

@Module
internal object FindChildrenInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideFindChildrenInteractor(childrenRepository: Lazy<ChildrenRepository>): FindChildrenInteractor {
        return ::findChildren.partially1(childrenRepository)
    }
}

@Module
internal object FindChildInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideFindChildInteractor(childrenRepository: Lazy<ChildrenRepository>): FindChildInteractor {
        return ::findChild.partially1(childrenRepository)
    }
}

@Module
internal object FindLastSearchResultsInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideFindLastSearchResultsInteractor(childrenRepository: Lazy<ChildrenRepository>): FindLastSearchResultsInteractor {
        return ::findLastSearchResults.partially1(childrenRepository)
    }
}

@Module
internal object ObserveInternetConnectivityInteractorModule {
    @Provides
    @Reusable
    @ObserveInternetConnectivityInteractorQualifier
    @JvmStatic
    fun provideObserveInternetConnectivityInteractor(
            connectivityManager: Lazy<ConnectivityManager>
    ): ObserveInternetConnectivityInteractor {
        return ::observeInternetConnectivity.partially1(connectivityManager)
    }
}

@Module
internal object CheckInternetConnectivityInteractorModule {
    @Provides
    @Reusable
    @CheckInternetConnectivityInteractorQualifier
    @JvmStatic
    fun provideCheckInternetConnectivityInteractor(connectivityManager: Lazy<ConnectivityManager>): CheckInternetConnectivityInteractor {
        return ::checkInternetConnectivity.partially1(connectivityManager)
    }
}

@Module(includes = [BusModule::class])
internal object ObserveChildPublishingStateInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideObserveChildPublishingStateInteractor(bus: Lazy<Bus>): ObserveChildPublishingStateInteractor {
        return ::observeChildPublishingState.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object CheckChildPublishingStateInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideCheckChildPublishingStateInteractor(bus: Lazy<Bus>): CheckChildPublishingStateInteractor {
        return ::checkChildPublishingState.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object NotifyChildPublishingStateChangeInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideNotifyChildPublishingStateChangeInteractor(bus: Lazy<Bus>): NotifyChildPublishingStateChangeInteractor {
        return ::notifyChildPublishingStateChange.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object NotifyChildFindingStateChangeInteractorModule {
    @Provides
    @Reusable
    @NotifyChildFindingStateChangeInteractorQualifier
    @JvmStatic
    fun provideNotifyChildFindingStateChangeInteractor(bus: Lazy<Bus>): NotifyChildFindingStateChangeInteractor {
        return ::notifyChildFindingStateChange.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object NotifyChildrenFindingStateChangeInteractorModule {
    @Provides
    @Reusable
    @NotifyChildrenFindingStateChangeInteractorQualifier
    @JvmStatic
    fun provideNotifyChildrenFindingStateChangeInteractor(bus: Lazy<Bus>): NotifyChildrenFindingStateChangeInteractor {
        return ::notifyChildrenFindingStateChange.partially1(bus)
    }
}

@Module
internal object ObserveUserAuthStateInteractorModule {
    @Provides
    @Reusable
    @ObserveUserAuthStateInteractorQualifier
    @JvmStatic
    fun provideObserveUserAuthStateInteractor(authManager: Lazy<AuthManager>): ObserveUserAuthStateInteractor {
        return ::observeUserAuthState.partially1(authManager)
    }
}

@Module
internal object ObserveSignedInUserInteractorModule {
    @Provides
    @Reusable
    @ObserveSignedInUserInteractorQualifier
    @JvmStatic
    fun provideObserveSignedInUserInteractor(authManager: Lazy<AuthManager>): ObserveSignedInUserInteractor {
        return ::observeSignedInUser.partially1(authManager)
    }
}

@Module
internal object SendPasswordResetEmailInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideSendPasswordResetEmailInteractor(authManager: Lazy<AuthManager>): SendPasswordResetEmailInteractor {
        return ::sendPasswordResetEmail.partially1(authManager)
    }
}

@Module
internal object SignInInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideSignInInteractor(authManager: Lazy<AuthManager>): SignInInteractor {
        return ::signIn.partially1(authManager)
    }
}

@Module
internal object SignInWithFacebookInteractorModule {
    @Provides
    @Reusable
    @SignInWithFacebookInteractorQualifier
    @JvmStatic
    fun provideSignInWithFacebookInteractor(authManager: Lazy<AuthManager>): SignInWithFacebookInteractor {
        return ::signInWithFacebook.partially1(authManager)
    }
}

@Module
internal object SignInWithGoogleInteractorModule {
    @Provides
    @Reusable
    @SignInWithGoogleInteractorQualifier
    @JvmStatic
    fun provideSignInWithGoogleInteractor(authManager: Lazy<AuthManager>): SignInWithGoogleInteractor {
        return ::signInWithGoogle.partially1(authManager)
    }
}

@Module
internal object SignInWithTwitterInteractorModule {
    @Provides
    @Reusable
    @SignInWithTwitterInteractorQualifier
    @JvmStatic
    fun provideSignInWithTwitterInteractor(authManager: Lazy<AuthManager>): SignInWithTwitterInteractor {
        return ::signInWithTwitter.partially1(authManager)
    }
}

@Module
internal object SignOutInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideSignOutInteractor(authManager: Lazy<AuthManager>): SignOutInteractor {
        return ::signOut.partially1(authManager)
    }
}

@Module
internal object SignUpInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideSignUpInteractor(authManager: Lazy<AuthManager>): SignUpInteractor {
        return ::signUp.partially1(authManager)
    }
}

@Module
internal object CompleteSignUpInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideCompleteSignUpInteractor(authManager: Lazy<AuthManager>): CompleteSignUpInteractor {
        return ::completeSignUp.partially1(authManager)
    }
}
