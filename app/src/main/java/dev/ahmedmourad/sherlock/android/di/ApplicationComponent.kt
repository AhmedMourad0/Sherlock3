package dev.ahmedmourad.sherlock.android.di

import dagger.Component
import dev.ahmedmourad.sherlock.android.services.SherlockService
import dev.ahmedmourad.sherlock.android.view.activity.MainActivity
import dev.ahmedmourad.sherlock.android.view.fragments.auth.*
import dev.ahmedmourad.sherlock.android.view.fragments.children.*
import dev.ahmedmourad.sherlock.android.widget.AppWidget
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsService
import dev.ahmedmourad.sherlock.auth.di.AuthModule
import dev.ahmedmourad.sherlock.children.di.ChildrenModule
import dev.ahmedmourad.sherlock.domain.di.DomainModule
import dev.ahmedmourad.sherlock.platform.di.PlatformModule
import javax.inject.Singleton

@Component(modules = [
    DomainModule::class,
    PlatformModule::class,
    ChildrenModule::class,
    AuthModule::class,
    AppModule::class
])
@Singleton
internal interface ApplicationComponent {
    fun inject(target: MainActivity)
    fun inject(target: HomeFragment)
    fun inject(target: AddChildFragment)
    fun inject(target: ChildDetailsFragment)
    fun inject(target: FindChildrenFragment)
    fun inject(target: ChildrenSearchResultsFragment)
    fun inject(target: OngoingInvestigationsFragment)
    fun inject(target: SignInFragment)
    fun inject(target: SignUpFragment)
    fun inject(target: CompleteSignUpFragment)
    fun inject(target: ResetPasswordFragment)
    fun inject(target: SignedInUserProfileFragment)
    fun inject(target: SherlockService)
    fun inject(target: ChildrenRemoteViewsService)
    fun inject(target: AppWidget)
}
