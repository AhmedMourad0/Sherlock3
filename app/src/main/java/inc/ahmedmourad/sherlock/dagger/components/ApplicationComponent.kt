package inc.ahmedmourad.sherlock.dagger.components

import dagger.Component
import inc.ahmedmourad.sherlock.auth.dagger.AuthModule
import inc.ahmedmourad.sherlock.children.dagger.ChildrenModule
import inc.ahmedmourad.sherlock.domain.dagger.DomainModule
import inc.ahmedmourad.sherlock.platform.dagger.PlatformModule
import javax.inject.Singleton

@Component(modules = [
    DomainModule::class,
    PlatformModule::class,
    ChildrenModule::class,
    AuthModule::class
])
@Singleton
internal interface ApplicationComponent {

    fun plusMainActivityComponent(): MainActivityComponent

    fun plusHomeFragmentComponent(): HomeComponent

    fun plusAddChildFragmentComponent(): AddChildComponent

    fun plusChildDetailsFragmentComponent(): ChildDetailsComponent

    fun plusFindChildrenFragmentComponent(): FindChildrenComponent

    fun plusChildrenSearchResultsFragmentComponent(): ChildrenSearchResultsComponent

    fun plusSignInFragmentComponent(): SignInComponent

    fun plusSignUpFragmentComponent(): SignUpComponent

    fun plusCompleteSignUpFragmentComponent(): CompleteSignUpComponent

    fun plusResetPasswordFragmentComponent(): ResetPasswordComponent

    fun plusSignedInUserProfileFragmentComponent(): SignedInUserProfileComponent

    fun plusSherlockServiceComponent(): SherlockServiceComponent

    fun plusChildrenRemoteViewsServiceComponent(): ChildrenRemoteViewsServiceComponent

    fun plusAppWidgetComponent(): AppWidgetComponent

    fun plusTestComponent(): TestComponent
}
