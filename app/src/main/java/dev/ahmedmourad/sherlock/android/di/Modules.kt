package dev.ahmedmourad.sherlock.android.di

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.android.adapters.AppSectionsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.adapters.AppSectionsRecyclerAdapterFactoryImpl
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapterFactoryImpl
import dev.ahmedmourad.sherlock.android.services.SherlockServiceIntentFactory
import dev.ahmedmourad.sherlock.android.services.SherlockServiceIntentFactoryImpl
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatterImpl
import dev.ahmedmourad.sherlock.android.utils.pickers.images.EsafirmImagePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.places.GooglePlacePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.viewmodel.activity.MainActivityViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.*
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.*
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsFactoryFactory
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsFactoryFactoryImpl
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsServiceIntentFactory
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsServiceIntentFactoryImpl

@Module
internal interface AppBindingsModule {

    @Binds
    fun bindChildrenRecyclerAdapterFactory(
            factory: ChildrenRecyclerAdapterFactoryImpl
    ): ChildrenRecyclerAdapterFactory

    @Binds
    fun bindAppSectionsRecyclerAdapterFactory(
            factory: AppSectionsRecyclerAdapterFactoryImpl
    ): AppSectionsRecyclerAdapterFactory

    @Binds
    fun bindSherlockServiceIntentFactory(
            factory: SherlockServiceIntentFactoryImpl
    ): SherlockServiceIntentFactory

    @Binds
    fun bindPlacePicker(
            picker: GooglePlacePicker
    ): PlacePicker

    @Binds
    fun bindImagePicker(
            picker: EsafirmImagePicker
    ): ImagePicker

    @Binds
    fun bindTextFormatter(
            formatter: TextFormatterImpl
    ): TextFormatter

    @Binds
    fun bindChildrenRemoteViewsServiceIntentFactory(
            factory: ChildrenRemoteViewsServiceIntentFactoryImpl
    ): ChildrenRemoteViewsServiceIntentFactory

    @Binds
    fun bindChildrenRemoteViewsFactoryFactory(
            factory: ChildrenRemoteViewsFactoryFactoryImpl
    ): ChildrenRemoteViewsFactoryFactory

    @Binds
    fun bindGlobalViewModel(
            factory: GlobalViewModel.Factory
    ): AssistedViewModelFactory<GlobalViewModel>

    @Binds
    fun bindMainActivityViewModel(
            factory: MainActivityViewModel.Factory
    ): AssistedViewModelFactory<MainActivityViewModel>

    @Binds
    fun bindAddChildViewModel(
            factory: AddChildViewModel.Factory
    ): AssistedViewModelFactory<AddChildViewModel>

    @Binds
    fun bindHomeViewModel(
            factory: HomeViewModel.Factory
    ): AssistedViewModelFactory<HomeViewModel>

    @Binds
    fun bindFindChildrenViewModel(
            factory: FindChildrenViewModel.Factory
    ): AssistedViewModelFactory<FindChildrenViewModel>

    @Binds
    fun bindResetPasswordViewModel(
            factory: ResetPasswordViewModel.Factory
    ): AssistedViewModelFactory<ResetPasswordViewModel>

    @Binds
    fun bindSignedInUserProfileViewModel(
            factory: SignedInUserProfileViewModel.Factory
    ): AssistedViewModelFactory<SignedInUserProfileViewModel>

    @Binds
    fun bindSignInViewModel(
            factory: SignInViewModel.Factory
    ): AssistedViewModelFactory<SignInViewModel>

    @Binds
    fun bindSignUpViewModel(
            factory: SignUpViewModel.Factory
    ): AssistedViewModelFactory<SignUpViewModel>

    @Binds
    fun bindCompleteSignUpViewModel(
            factory: CompleteSignUpViewModel.Factory
    ): AssistedViewModelFactory<CompleteSignUpViewModel>

    @Binds
    fun bindChildrenSearchResultsViewModel(
            factory: ChildrenSearchResultsViewModel.Factory
    ): AssistedViewModelFactory<ChildrenSearchResultsViewModel>

    @Binds
    fun bindChildDetailsViewModel(
            factory: ChildDetailsViewModel.Factory
    ): AssistedViewModelFactory<ChildDetailsViewModel>
}
