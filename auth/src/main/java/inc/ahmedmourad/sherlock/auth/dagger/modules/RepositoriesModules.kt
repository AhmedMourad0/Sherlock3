package inc.ahmedmourad.sherlock.auth.dagger.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.auth.authenticator.AuthFirebaseAuthenticator
import inc.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.AuthFirebaseFirestoreQualifier
import inc.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.AuthFirebaseStorageQualifier
import inc.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.IsUserSignedInQualifier
import inc.ahmedmourad.sherlock.auth.images.repository.AuthFirebaseStorageImageRepository
import inc.ahmedmourad.sherlock.auth.manager.ObserveUserAuthState
import inc.ahmedmourad.sherlock.auth.manager.SherlockAuthManager
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthAuthenticator
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthImageRepository
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthRemoteRepository
import inc.ahmedmourad.sherlock.auth.remote.repository.AuthFirebaseFirestoreRemoteRepository
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager

@Module(includes = [
    AuthAuthenticatorModule::class,
    AuthRemoteRepositoryModule::class,
    AuthImageRepositoryModule::class
])
internal object AuthManagerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideAuthManager(
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
    fun provideIsUserSignedIn(
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
    fun provideAuthAuthenticator(
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
    fun provideAuthRemoteRepository(
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
    fun provideAuthImageRepository(
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
