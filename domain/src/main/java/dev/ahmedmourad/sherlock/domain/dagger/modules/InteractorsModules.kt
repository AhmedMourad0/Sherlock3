package dev.ahmedmourad.sherlock.domain.dagger.modules

import arrow.syntax.function.partially1
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.*
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.interactors.auth.*
import dev.ahmedmourad.sherlock.domain.interactors.children.*
import dev.ahmedmourad.sherlock.domain.interactors.common.*
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager

@Module
internal object AddChildInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(childrenRepository: Lazy<ChildrenRepository>): AddChildInteractor {
        return ::addChild.partially1(childrenRepository)
    }
}

@Module
internal object FindChildrenInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(childrenRepository: Lazy<ChildrenRepository>): FindChildrenInteractor {
        return ::findChildren.partially1(childrenRepository)
    }
}

@Module
internal object FindChildInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(childrenRepository: Lazy<ChildrenRepository>): FindChildInteractor {
        return ::findChild.partially1(childrenRepository)
    }
}

@Module
internal object FindLastSearchResultsInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(childrenRepository: Lazy<ChildrenRepository>): FindLastSearchResultsInteractor {
        return ::findLastSearchResults.partially1(childrenRepository)
    }
}

@Module
internal object ObserveInternetConnectivityInteractorModule {
    @Provides
    @Reusable
    @ObserveInternetConnectivityInteractorQualifier
    @JvmStatic
    fun provide(
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
    fun provide(connectivityManager: Lazy<ConnectivityManager>): CheckInternetConnectivityInteractor {
        return ::checkInternetConnectivity.partially1(connectivityManager)
    }
}

@Module(includes = [BusModule::class])
internal object ObserveChildPublishingStateInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(bus: Lazy<Bus>): ObserveChildPublishingStateInteractor {
        return ::observeChildPublishingState.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object CheckChildPublishingStateInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(bus: Lazy<Bus>): CheckChildPublishingStateInteractor {
        return ::checkChildPublishingState.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object NotifyChildPublishingStateChangeInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(bus: Lazy<Bus>): NotifyChildPublishingStateChangeInteractor {
        return ::notifyChildPublishingStateChange.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object NotifyChildFindingStateChangeInteractorModule {
    @Provides
    @Reusable
    @NotifyChildFindingStateChangeInteractorQualifier
    @JvmStatic
    fun provide(bus: Lazy<Bus>): NotifyChildFindingStateChangeInteractor {
        return ::notifyChildFindingStateChange.partially1(bus)
    }
}

@Module(includes = [BusModule::class])
internal object NotifyChildrenFindingStateChangeInteractorModule {
    @Provides
    @Reusable
    @NotifyChildrenFindingStateChangeInteractorQualifier
    @JvmStatic
    fun provide(bus: Lazy<Bus>): NotifyChildrenFindingStateChangeInteractor {
        return ::notifyChildrenFindingStateChange.partially1(bus)
    }
}

@Module
internal object ObserveUserAuthStateInteractorModule {
    @Provides
    @Reusable
    @ObserveUserAuthStateInteractorQualifier
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): ObserveUserAuthStateInteractor {
        return ::observeUserAuthState.partially1(authManager)
    }
}

@Module
internal object ObserveSignedInUserInteractorModule {
    @Provides
    @Reusable
    @ObserveSignedInUserInteractorQualifier
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): ObserveSignedInUserInteractor {
        return ::observeSignedInUser.partially1(authManager)
    }
}

@Module
internal object SendPasswordResetEmailInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SendPasswordResetEmailInteractor {
        return ::sendPasswordResetEmail.partially1(authManager)
    }
}

@Module
internal object SignInInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SignInInteractor {
        return ::signIn.partially1(authManager)
    }
}

@Module
internal object SignInWithFacebookInteractorModule {
    @Provides
    @Reusable
    @SignInWithFacebookInteractorQualifier
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SignInWithFacebookInteractor {
        return ::signInWithFacebook.partially1(authManager)
    }
}

@Module
internal object SignInWithGoogleInteractorModule {
    @Provides
    @Reusable
    @SignInWithGoogleInteractorQualifier
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SignInWithGoogleInteractor {
        return ::signInWithGoogle.partially1(authManager)
    }
}

@Module
internal object SignInWithTwitterInteractorModule {
    @Provides
    @Reusable
    @SignInWithTwitterInteractorQualifier
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SignInWithTwitterInteractor {
        return ::signInWithTwitter.partially1(authManager)
    }
}

@Module
internal object SignOutInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SignOutInteractor {
        return ::signOut.partially1(authManager)
    }
}

@Module
internal object SignUpInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): SignUpInteractor {
        return ::signUp.partially1(authManager)
    }
}

@Module
internal object CompleteSignUpInteractorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(authManager: Lazy<AuthManager>): CompleteSignUpInteractor {
        return ::completeSignUp.partially1(authManager)
    }
}
