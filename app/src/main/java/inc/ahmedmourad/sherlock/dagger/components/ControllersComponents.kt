package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.*
import inc.ahmedmourad.sherlock.view.controllers.HomeController
import inc.ahmedmourad.sherlock.view.controllers.auth.*
import inc.ahmedmourad.sherlock.view.controllers.children.AddChildController
import inc.ahmedmourad.sherlock.view.controllers.children.ChildDetailsController
import inc.ahmedmourad.sherlock.view.controllers.children.ChildrenSearchResultsController
import inc.ahmedmourad.sherlock.view.controllers.children.FindChildrenController

@Subcomponent(modules = [
    FindChildrenControllerModule::class,
    AddChildControllerModule::class,
    AppSectionsRecyclerAdapterModule::class
])
internal interface HomeComponent {
    fun inject(controller: HomeController)
}

@Subcomponent(modules = [
    AddChildViewModelModule::class,
    ChildDetailsControllerModule::class,
    PlacePickerModule::class,
    ImagePickerModule::class
])
internal interface AddChildComponent {
    fun inject(controller: AddChildController)
}

@Subcomponent(modules = [
    TextFormatterModule::class,
    ChildDetailsViewModelModule::class
])
internal interface ChildDetailsComponent {
    fun inject(controller: ChildDetailsController)
}

@Subcomponent(modules = [
    FindChildrenViewModelModule::class,
    ChildrenSearchResultsControllerModule::class,
    PlacePickerModule::class
])
internal interface FindChildrenComponent {
    fun inject(controller: FindChildrenController)
}

@Subcomponent(modules = [
    TextFormatterModule::class,
    ChildrenRecyclerAdapterModule::class,
    ChildDetailsControllerModule::class,
    ChildrenSearchResultsViewModelModule::class
])
internal interface ChildrenSearchResultsComponent {
    fun inject(controller: ChildrenSearchResultsController)
}

@Subcomponent(modules = [
    SignInViewModelModule::class,
    SignUpControllerModule::class,
    ResetPasswordControllerModule::class,
    SignedInUserProfileControllerModule::class,
    CompleteSignUpControllerModule::class
])
internal interface SignInComponent {
    fun inject(controller: SignInController)
}

@Subcomponent(modules = [
    SignUpViewModelModule::class,
    SignInControllerModule::class,
    SignedInUserProfileControllerModule::class,
    CompleteSignUpControllerModule::class,
    ImagePickerModule::class
])
internal interface SignUpComponent {
    fun inject(controller: SignUpController)
}

@Subcomponent(modules = [
    CompleteSignUpViewModelModule::class,
    ImagePickerModule::class,
    SignedInUserProfileControllerModule::class
])
internal interface CompleteSignUpComponent {
    fun inject(controller: CompleteSignUpController)
}

@Subcomponent(modules = [
    ResetPasswordViewModelModule::class,
    SignInControllerModule::class
])
internal interface ResetPasswordComponent {
    fun inject(controller: ResetPasswordController)
}

@Subcomponent(modules = [
    SignedInUserProfileViewModelModule::class,
    CompleteSignUpControllerModule::class,
    SignInControllerModule::class
])
internal interface SignedInUserProfileComponent {
    fun inject(controller: SignedInUserProfileController)
}
