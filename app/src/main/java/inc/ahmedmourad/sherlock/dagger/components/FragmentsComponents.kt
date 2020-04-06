package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.*
import inc.ahmedmourad.sherlock.view.fragments.HomeFragment
import inc.ahmedmourad.sherlock.view.fragments.auth.*
import inc.ahmedmourad.sherlock.view.fragments.children.AddChildFragment
import inc.ahmedmourad.sherlock.view.fragments.children.ChildDetailsFragment
import inc.ahmedmourad.sherlock.view.fragments.children.ChildrenSearchResultsFragment
import inc.ahmedmourad.sherlock.view.fragments.children.FindChildrenFragment

@Subcomponent(modules = [
    AppSectionsRecyclerAdapterModule::class
])
internal interface HomeComponent {
    fun inject(fragment: HomeFragment)
}

@Subcomponent(modules = [
    AddChildViewModelModule::class,
    PlacePickerModule::class,
    ImagePickerModule::class
])
internal interface AddChildComponent {
    fun inject(fragment: AddChildFragment)
}

@Subcomponent(modules = [
    TextFormatterModule::class,
    ChildDetailsViewModelModule::class
])
internal interface ChildDetailsComponent {
    fun inject(fragment: ChildDetailsFragment)
}

@Subcomponent(modules = [
    FindChildrenViewModelModule::class,
    PlacePickerModule::class
])
internal interface FindChildrenComponent {
    fun inject(fragment: FindChildrenFragment)
}

@Subcomponent(modules = [
    TextFormatterModule::class,
    ChildrenRecyclerAdapterModule::class,
    ChildrenSearchResultsViewModelModule::class
])
internal interface ChildrenSearchResultsComponent {
    fun inject(fragment: ChildrenSearchResultsFragment)
}

@Subcomponent(modules = [
    SignInViewModelModule::class
])
internal interface SignInComponent {
    fun inject(fragment: SignInFragment)
}

@Subcomponent(modules = [
    SignUpViewModelModule::class,
    ImagePickerModule::class
])
internal interface SignUpComponent {
    fun inject(fragment: SignUpFragment)
}

@Subcomponent(modules = [
    CompleteSignUpViewModelModule::class,
    ImagePickerModule::class
])
internal interface CompleteSignUpComponent {
    fun inject(fragment: CompleteSignUpFragment)
}

@Subcomponent(modules = [
    ResetPasswordViewModelModule::class
])
internal interface ResetPasswordComponent {
    fun inject(fragment: ResetPasswordFragment)
}

@Subcomponent(modules = [
    SignedInUserProfileViewModelModule::class
])
internal interface SignedInUserProfileComponent {
    fun inject(fragment: SignedInUserProfileFragment)
}
