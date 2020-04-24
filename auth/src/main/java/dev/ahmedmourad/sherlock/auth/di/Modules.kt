package dev.ahmedmourad.sherlock.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.authenticator.FirebaseAuthenticator
import dev.ahmedmourad.sherlock.auth.images.repository.FirebaseStorageImageRepository
import dev.ahmedmourad.sherlock.auth.manager.AuthManagerImpl
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.auth.manager.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.UserAuthStateObservable
import dev.ahmedmourad.sherlock.auth.remote.repository.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.domain.data.AuthManager

@Module(includes = [
    FirebaseFirestoreModule::class,
    FirebaseAuthModule::class,
    FirebaseStorageModule::class
])
internal interface AuthBindingsModule {

    @Binds
    fun bindAuthManager(
            manager: AuthManagerImpl
    ): AuthManager

    @Binds
    @InternalApi
    fun bindAuthenticator(
            authenticator: FirebaseAuthenticator
    ): Authenticator

    @Binds
    @InternalApi
    fun bindUserAuthStateObservable(
            @InternalApi authenticator: Authenticator
    ): UserAuthStateObservable

    @Binds
    @InternalApi
    fun bindRemoteRepository(
            remoteRepository: FirebaseFirestoreRemoteRepository
    ): RemoteRepository

    @Binds
    @InternalApi
    fun bindImageRepository(
            imageRepository: FirebaseStorageImageRepository
    ): ImageRepository
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

@Module
internal object FirebaseAuthModule {
    @Provides
    @Reusable
    @InternalApi
    @JvmStatic
    fun provide(): FirebaseAuth = FirebaseAuth.getInstance()
}
