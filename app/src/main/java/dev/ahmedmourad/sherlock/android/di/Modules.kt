package dev.ahmedmourad.sherlock.android.di

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.android.adapters.AppSectionsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.adapters.AppSectionsRecyclerAdapterFactoryImpl
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapterFactoryImpl
import dev.ahmedmourad.sherlock.android.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.formatter.TextFormatterImpl
import dev.ahmedmourad.sherlock.android.loader.GlideImageLoader
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.pickers.images.EsafirmImagePicker
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.pickers.places.PingPlacePicker
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.services.SherlockServiceIntentFactory
import dev.ahmedmourad.sherlock.android.services.SherlockServiceIntentFactoryImpl
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
            impl: ChildrenRecyclerAdapterFactoryImpl
    ): ChildrenRecyclerAdapterFactory

    @Binds
    fun bindAppSectionsRecyclerAdapterFactory(
            impl: AppSectionsRecyclerAdapterFactoryImpl
    ): AppSectionsRecyclerAdapterFactory

    @Binds
    fun bindSherlockServiceIntentFactory(
            impl: SherlockServiceIntentFactoryImpl
    ): SherlockServiceIntentFactory

    @Binds
    fun bindPlacePicker(
            impl: PingPlacePicker
    ): PlacePicker

    @Binds
    fun bindImagePicker(
            impl: EsafirmImagePicker
    ): ImagePicker

    @Binds
    fun bindTextFormatter(
            impl: TextFormatterImpl
    ): TextFormatter

    @Binds
    fun bindImageLoader(
            impl: GlideImageLoader
    ): ImageLoader

    @Binds
    fun bindChildrenRemoteViewsServiceIntentFactory(
            impl: ChildrenRemoteViewsServiceIntentFactoryImpl
    ): ChildrenRemoteViewsServiceIntentFactory

    @Binds
    fun bindChildrenRemoteViewsFactoryFactory(
            impl: ChildrenRemoteViewsFactoryFactoryImpl
    ): ChildrenRemoteViewsFactoryFactory

    @Binds
    fun bindGlobalViewModel(
            impl: GlobalViewModel.Factory
    ): AssistedViewModelFactory<GlobalViewModel>

    @Binds
    fun bindMainActivityViewModel(
            impl: MainActivityViewModel.Factory
    ): AssistedViewModelFactory<MainActivityViewModel>

    @Binds
    fun bindAddChildViewModel(
            impl: AddChildViewModel.Factory
    ): AssistedViewModelFactory<AddChildViewModel>

    @Binds
    fun bindHomeViewModel(
            impl: HomeViewModel.Factory
    ): AssistedViewModelFactory<HomeViewModel>

    @Binds
    fun bindFindChildrenViewModel(
            impl: FindChildrenViewModel.Factory
    ): AssistedViewModelFactory<FindChildrenViewModel>

    @Binds
    fun bindResetPasswordViewModel(
            impl: ResetPasswordViewModel.Factory
    ): AssistedViewModelFactory<ResetPasswordViewModel>

    @Binds
    fun bindSignedInUserProfileViewModel(
            impl: SignedInUserProfileViewModel.Factory
    ): AssistedViewModelFactory<SignedInUserProfileViewModel>

    @Binds
    fun bindSignInViewModel(
            impl: SignInViewModel.Factory
    ): AssistedViewModelFactory<SignInViewModel>

    @Binds
    fun bindSignUpViewModel(
            impl: SignUpViewModel.Factory
    ): AssistedViewModelFactory<SignUpViewModel>

    @Binds
    fun bindCompleteSignUpViewModel(
            impl: CompleteSignUpViewModel.Factory
    ): AssistedViewModelFactory<CompleteSignUpViewModel>

    @Binds
    fun bindChildrenSearchResultsViewModel(
            impl: ChildrenSearchResultsViewModel.Factory
    ): AssistedViewModelFactory<ChildrenSearchResultsViewModel>

    @Binds
    fun bindChildDetailsViewModel(
            impl: ChildDetailsViewModel.Factory
    ): AssistedViewModelFactory<ChildDetailsViewModel>
}
