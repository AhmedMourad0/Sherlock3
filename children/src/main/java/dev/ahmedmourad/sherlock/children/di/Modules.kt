package dev.ahmedmourad.sherlock.children.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.images.repository.FirebaseStorageImageRepository
import dev.ahmedmourad.sherlock.children.local.database.ChildrenRoomDatabase
import dev.ahmedmourad.sherlock.children.local.repository.RoomLocalRepository
import dev.ahmedmourad.sherlock.children.remote.repositories.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.children.repository.ChildrenRepositoryImpl
import dev.ahmedmourad.sherlock.children.repository.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository

@Module(includes = [
    ChildrenRoomDatabaseModule::class,
    FirebaseFirestoreModule::class,
    FirebaseStorageModule::class
])
internal interface ChildrenBindingsModule {

    @Binds
    fun bindChildrenRepository(
            repository: ChildrenRepositoryImpl
    ): ChildrenRepository

    @Binds
    @InternalApi
    fun bindRemoteRepository(
            repository: FirebaseFirestoreRemoteRepository
    ): RemoteRepository

    @Binds
    @InternalApi
    fun bindImageRepository(
            repository: FirebaseStorageImageRepository
    ): ImageRepository

    @Binds
    @InternalApi
    fun bindLocalRepository(
            repository: RoomLocalRepository
    ): LocalRepository
}

@Module
internal object ChildrenRoomDatabaseModule {
    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provide(): ChildrenRoomDatabase = ChildrenRoomDatabase.getInstance()
}

@Module
internal object FirebaseFirestoreModule {
    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provide(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
internal object FirebaseStorageModule {
    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provide(): FirebaseStorage = FirebaseStorage.getInstance()
}
