package dev.ahmedmourad.sherlock.auth.di

import arrow.core.orNull
import arrow.core.toOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.authenticator.FirebaseAuthenticator
import dev.ahmedmourad.sherlock.auth.authenticator.messaging.CloudMessenger
import dev.ahmedmourad.sherlock.auth.authenticator.messaging.FirebaseCloudMessenger
import dev.ahmedmourad.sherlock.auth.images.repository.FirebaseStorageImageRepository
import dev.ahmedmourad.sherlock.auth.manager.AuthManagerImpl
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.auth.manager.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.auth.remote.repository.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.FindSimpleUsers
import dev.ahmedmourad.sherlock.domain.data.ObserveSimpleSignedInUser
import dev.ahmedmourad.sherlock.domain.data.ObserveUserAuthState

@Module
internal interface AuthBindingsModule {

    @Binds
    fun bindAuthManager(
            impl: AuthManagerImpl
    ): AuthManager

    @Binds
    @InternalApi
    fun bindAuthenticator(
            impl: FirebaseAuthenticator
    ): Authenticator

    @Binds
    @InternalApi
    fun bindCloudMessenger(
            impl: FirebaseCloudMessenger
    ): CloudMessenger

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
}

@Module
internal object AuthProvidedModules {

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
    @Reusable
    @InternalApi
    @JvmStatic
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Reusable
    @JvmStatic
    fun provideObserveUserAuthState(
            authManager: AuthManager
    ): ObserveUserAuthState = authManager::observeUserAuthState

    @Provides
    @Reusable
    @JvmStatic
    fun provideObserveSimpleSignedInUser(
            authManager: AuthManager
    ): ObserveSimpleSignedInUser = {
        authManager.observeSignedInUser().map { either ->
            either.toOption().flatMap { user ->
                user?.orNull()?.simplify().toOption()
            }
        }
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFindSimpleUsersByIds(
            authManager: AuthManager
    ): FindSimpleUsers = authManager::findSimpleUsers
}
