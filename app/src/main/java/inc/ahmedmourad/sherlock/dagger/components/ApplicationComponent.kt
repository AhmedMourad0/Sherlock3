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

    fun plusHomeControllerComponent(): HomeComponent

    fun plusAddChildControllerComponent(): AddChildComponent

    fun plusChildDetailsControllerComponent(): ChildDetailsComponent

    fun plusFindChildrenControllerComponent(): FindChildrenComponent

    fun plusChildrenSearchResultsControllerComponent(): ChildrenSearchResultsComponent

    fun plusSignInControllerComponent(): SignInComponent

    fun plusSignUpControllerComponent(): SignUpComponent

    fun plusCompleteSignUpControllerComponent(): CompleteSignUpComponent

    fun plusResetPasswordControllerComponent(): ResetPasswordComponent

    fun plusSignedInUserProfileControllerComponent(): SignedInUserProfileComponent

    fun plusSherlockServiceComponent(): SherlockServiceComponent

    fun plusChildrenRemoteViewsServiceComponent(): ChildrenRemoteViewsServiceComponent

    fun plusAppWidgetComponent(): AppWidgetComponent

    fun plusTestComponent(): TestComponent
}
