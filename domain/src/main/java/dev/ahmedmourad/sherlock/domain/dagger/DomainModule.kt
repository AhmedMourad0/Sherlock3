package dev.ahmedmourad.sherlock.domain.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.domain.dagger.modules.*

@Module(includes = [
    FilterModule::class,
    BusModule::class,
    AddChildInteractorModule::class,
    FindChildrenInteractorModule::class,
    FindChildInteractorModule::class,
    FindLastSearchResultsInteractorModule::class,
    ObserveInternetConnectivityInteractorModule::class,
    CheckInternetConnectivityInteractorModule::class,
    ObserveChildPublishingStateInteractorModule::class,
    CheckChildPublishingStateInteractorModule::class,
    NotifyChildPublishingStateChangeInteractorModule::class,
    NotifyChildFindingStateChangeInteractorModule::class,
    NotifyChildrenFindingStateChangeInteractorModule::class,
    ObserveUserAuthStateInteractorModule::class,
    ObserveSignedInUserInteractorModule::class,
    SendPasswordResetEmailInteractorModule::class,
    SignInInteractorModule::class,
    SignInWithFacebookInteractorModule::class,
    SignInWithGoogleInteractorModule::class,
    SignInWithTwitterInteractorModule::class,
    SignOutInteractorModule::class,
    SignUpInteractorModule::class,
    CompleteSignUpInteractorModule::class
])
object DomainModule
