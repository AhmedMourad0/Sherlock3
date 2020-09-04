package dev.ahmedmourad.sherlock.children.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.sqldelight.db.SqlDriver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.images.repository.FirebaseStorageImageRepository
import dev.ahmedmourad.sherlock.children.local.ChildrenDatabase
import dev.ahmedmourad.sherlock.children.local.ChildrenQueries
import dev.ahmedmourad.sherlock.children.local.UsersQueries
import dev.ahmedmourad.sherlock.children.local.daos.ChildrenDao
import dev.ahmedmourad.sherlock.children.local.daos.ChildrenDaoImpl
import dev.ahmedmourad.sherlock.children.local.database.childrenDatabase
import dev.ahmedmourad.sherlock.children.local.database.sqliteDriver
import dev.ahmedmourad.sherlock.children.local.repository.LocalRepositoryImpl
import dev.ahmedmourad.sherlock.children.preferences.PreferencesManagerImpl
import dev.ahmedmourad.sherlock.children.remote.repositories.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.children.repository.ChildrenRepositoryImpl
import dev.ahmedmourad.sherlock.children.repository.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.PreferencesManager
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import javax.inject.Singleton

@Module
internal interface ChildrenBindingsModule {

    @Binds
    fun bindChildrenRepository(
            impl: ChildrenRepositoryImpl
    ): ChildrenRepository

    @Binds
    @InternalApi
    fun bindRemoteRepository(
            impl: FirebaseFirestoreRemoteRepository
    ): RemoteRepository

    @Binds
    @InternalApi
    fun bindImageRepository(
            impl: FirebaseStorageImageRepository
    ): ImageRepository

    @Binds
    @InternalApi
    fun bindLocalRepository(
            impl: LocalRepositoryImpl
    ): LocalRepository

    @Binds
    @InternalApi
    fun bindChildrenDao(
            impl: ChildrenDaoImpl
    ): ChildrenDao

    @Binds
    @InternalApi
    fun bindPreferencesManager(
            impl: PreferencesManagerImpl
    ): PreferencesManager
}

@Module
internal object ChildrenProvidedModule {

    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    @InternalApi
    @JvmStatic
    fun provideSqliteDriver(): SqlDriver {
        return sqliteDriver()
    }

    @Provides
    @Singleton
    @InternalApi
    @JvmStatic
    fun provideDeltaDatabase(@InternalApi driver: SqlDriver): ChildrenDatabase {
        return childrenDatabase(driver)
    }

    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provideChildrenQueries(@InternalApi db: ChildrenDatabase): ChildrenQueries {
        return db.childrenQueries
    }

    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provideUsersQueries(@InternalApi db: ChildrenDatabase): UsersQueries {
        return db.usersQueries
    }
}
