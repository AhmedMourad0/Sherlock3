package dev.ahmedmourad.sherlock.android.di

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.android.di.modules.factories.*
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatterImpl
import dev.ahmedmourad.sherlock.android.utils.pickers.images.EsafirmImagePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.places.GooglePlacePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.places.PlacePicker

@Module
internal interface AppBindingsModule {

    @Binds
    fun bindChildrenRecyclerAdapterFactory(
            factory: ChildrenRecyclerAdapterFactoryImpl
    ): ChildrenRecyclerAdapterFactory

    @Binds
    fun bindChildrenRecyclerAdapterFactory(
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
}