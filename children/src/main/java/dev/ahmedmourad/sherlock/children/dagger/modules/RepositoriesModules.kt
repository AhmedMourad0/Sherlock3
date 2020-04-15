package dev.ahmedmourad.sherlock.children.dagger.modules

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseFirestoreQualifier
import dev.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseStorageQualifier
import dev.ahmedmourad.sherlock.children.images.repository.ChildrenFirebaseStorageImageRepository
import dev.ahmedmourad.sherlock.children.local.database.SherlockDatabase
import dev.ahmedmourad.sherlock.children.local.repository.ChildrenRoomLocalRepository
import dev.ahmedmourad.sherlock.children.remote.repositories.ChildrenFirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.children.repository.SherlockChildrenRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.ChildrenImageRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.ChildrenLocalRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.ChildrenRemoteRepository
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.interactors.common.NotifyChildFindingStateChangeInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.NotifyChildPublishingStateChangeInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.NotifyChildrenFindingStateChangeInteractor
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager

@Module(includes = [
    ChildrenRemoteRepositoryModule::class,
    ChildrenLocalRepositoryModule::class,
    ChildrenImageRepositoryModule::class
])
internal object ChildrenRepositoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            childrenLocalRepository: Lazy<ChildrenLocalRepository>,
            childrenRemoteRepository: Lazy<ChildrenRemoteRepository>,
            childrenImageRepository: Lazy<ChildrenImageRepository>,
            notifyChildPublishingStateChangeInteractor: NotifyChildPublishingStateChangeInteractor,
            notifyChildFindingStateChangeInteractor: NotifyChildFindingStateChangeInteractor,
            notifyChildrenFindingStateChangeInteractor: NotifyChildrenFindingStateChangeInteractor
    ): ChildrenRepository {
        return SherlockChildrenRepository(
                childrenLocalRepository,
                childrenRemoteRepository,
                childrenImageRepository,
                notifyChildPublishingStateChangeInteractor,
                notifyChildFindingStateChangeInteractor,
                notifyChildrenFindingStateChangeInteractor
        )
    }
}

@Module(includes = [FirebaseFirestoreModule::class])
internal object ChildrenRemoteRepositoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            @ChildrenFirebaseFirestoreQualifier db: Lazy<FirebaseFirestore>,
            authManager: Lazy<AuthManager>,
            connectivityManager: Lazy<ConnectivityManager>
    ): ChildrenRemoteRepository {
        return ChildrenFirebaseFirestoreRemoteRepository(
                db,
                authManager,
                connectivityManager
        )
    }
}

@Module(includes = [FirebaseStorageModule::class])
internal object ChildrenImageRepositoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            connectivityManager: Lazy<ConnectivityManager>,
            authManager: Lazy<AuthManager>,
            @ChildrenFirebaseStorageQualifier storage: Lazy<FirebaseStorage>
    ): ChildrenImageRepository {
        return ChildrenFirebaseStorageImageRepository(
                connectivityManager,
                authManager,
                storage
        )
    }
}

@Module(includes = [SherlockDatabaseModule::class])
internal object ChildrenLocalRepositoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(db: Lazy<SherlockDatabase>): ChildrenLocalRepository = ChildrenRoomLocalRepository(db)
}
