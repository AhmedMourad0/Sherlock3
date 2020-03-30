package inc.ahmedmourad.sherlock.dagger

import inc.ahmedmourad.sherlock.dagger.components.ApplicationComponent
import inc.ahmedmourad.sherlock.dagger.components.DaggerApplicationComponent

internal object SherlockComponent {

    private val applicationComponent: ApplicationComponent = DaggerApplicationComponent.create()

    object Activities {
        val mainComponent = ComponentProvider(applicationComponent::plusMainActivityComponent)
    }

    object Controllers {
        val homeComponent = ComponentProvider(applicationComponent::plusHomeControllerComponent)
        val addChildComponent = ComponentProvider(applicationComponent::plusAddChildControllerComponent)
        val childDetailsComponent = ComponentProvider(applicationComponent::plusChildDetailsControllerComponent)
        val findChildrenComponent = ComponentProvider(applicationComponent::plusFindChildrenControllerComponent)
        val childrenSearchResultsComponent = ComponentProvider(applicationComponent::plusChildrenSearchResultsControllerComponent)
        val signInComponent = ComponentProvider(applicationComponent::plusSignInControllerComponent)
        val signUpComponent = ComponentProvider(applicationComponent::plusSignUpControllerComponent)
        val completeSignUpComponent = ComponentProvider(applicationComponent::plusCompleteSignUpControllerComponent)
        val resetPasswordComponent = ComponentProvider(applicationComponent::plusResetPasswordControllerComponent)
        val signedInUserProfileComponent = ComponentProvider(applicationComponent::plusSignedInUserProfileControllerComponent)
    }

    object Services {
        val sherlockServiceComponent = ComponentProvider(applicationComponent::plusSherlockServiceComponent)
    }

    object Widget {
        val childrenRemoteViewsServiceComponent = ComponentProvider(applicationComponent::plusChildrenRemoteViewsServiceComponent)
        val appWidgetComponent = ComponentProvider(applicationComponent::plusAppWidgetComponent)
    }

    object Test {
        val testComponent = ComponentProvider(applicationComponent::plusTestComponent)
    }
}

internal class ComponentProvider<T>(private val createComponent: () -> T) {

    private var component: T? = null

    fun get() = component ?: createComponent().also { component = it }

    fun release() {
        component = null
    }
}
