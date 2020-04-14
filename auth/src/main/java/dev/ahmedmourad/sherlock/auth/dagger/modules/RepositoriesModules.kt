package dev.ahmedmourad.sherlock.auth.dagger.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.authenticator.AuthFirebaseAuthenticator
import dev.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.AuthFirebaseFirestoreQualifier
import dev.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.AuthFirebaseStorageQualifier
import dev.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.IsUserSignedInQualifier
import dev.ahmedmourad.sherlock.auth.images.repository.AuthFirebaseStorageImageRepository
import dev.ahmedmourad.sherlock.auth.manager.ObserveUserAuthState
import dev.ahmedmourad.sherlock.auth.manager.SherlockAuthManager
import dev.ahmedmourad.sherlock.auth.manager.dependencies.AuthAuthenticator
import dev.ahmedmourad.sherlock.auth.manager.dependencies.AuthImageRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.AuthRemoteRepository
import dev.ahmedmourad.sherlock.auth.remote.repository.AuthFirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager

@Module(includes = [
    AuthAuthenticatorModule::class,
    AuthRemoteRepositoryModule::class,
    AuthImageRepositoryModule::class
])
internal object AuthManagerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            authenticator: Lazy<AuthAuthenticator>,
            usersRepository: Lazy<AuthRemoteRepository>,
            imageRepository: Lazy<AuthImageRepository>
    ): AuthManager {
        return SherlockAuthManager(
                authenticator,
                usersRepository,
                imageRepository
        )
    }
}

@Module(includes = [AuthManagerModule::class])
internal object IsUserSignedInModule {
    @Provides
    @Reusable
    @IsUserSignedInQualifier
    @JvmStatic
    fun provide(
            manager: Lazy<AuthManager>
    ): ObserveUserAuthState {
        return manager.get()::observeUserAuthState
    }
}

@Module(includes = [FirebaseAuthModule::class])
internal object AuthAuthenticatorModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            auth: Lazy<FirebaseAuth>,
            connectivityManager: Lazy<ConnectivityManager>
    ): AuthAuthenticator {
        return AuthFirebaseAuthenticator(auth, connectivityManager)
    }
}

@Module(includes = [
    FirebaseFirestoreModule::class,
    AuthImageRepositoryModule::class,
    IsUserSignedInModule::class
])
internal object AuthRemoteRepositoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            @AuthFirebaseFirestoreQualifier db: Lazy<FirebaseFirestore>,
            connectivityManager: Lazy<ConnectivityManager>,
            @IsUserSignedInQualifier observeUserAuthState: ObserveUserAuthState
    ): AuthRemoteRepository {
        return AuthFirebaseFirestoreRemoteRepository(
                db,
                connectivityManager,
                observeUserAuthState
        )
    }
}

@Module(includes = [
    FirebaseStorageModule::class,
    IsUserSignedInModule::class
])
internal object AuthImageRepositoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            connectivityManager: Lazy<ConnectivityManager>,
            @IsUserSignedInQualifier observeUserAuthState: ObserveUserAuthState,
            @AuthFirebaseStorageQualifier storage: Lazy<FirebaseStorage>
    ): AuthImageRepository {
        return AuthFirebaseStorageImageRepository(
                connectivityManager,
                observeUserAuthState,
                storage
        )
    }
}
