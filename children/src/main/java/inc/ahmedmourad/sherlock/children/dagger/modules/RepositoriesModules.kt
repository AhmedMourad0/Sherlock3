package inc.ahmedmourad.sherlock.children.dagger.modules

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseFirestoreQualifier
import inc.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseStorageQualifier
import inc.ahmedmourad.sherlock.children.images.repository.ChildrenFirebaseStorageImageRepository
import inc.ahmedmourad.sherlock.children.local.database.SherlockDatabase
import inc.ahmedmourad.sherlock.children.local.repository.ChildrenRoomLocalRepository
import inc.ahmedmourad.sherlock.children.remote.repositories.ChildrenFirebaseFirestoreRemoteRepository
import inc.ahmedmourad.sherlock.children.repository.SherlockChildrenRepository
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenImageRepository
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenLocalRepository
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenRemoteRepository
import inc.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.NotifyChildFindingStateChangeInteractorQualifier
import inc.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.NotifyChildrenFindingStateChangeInteractorQualifier
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.interactors.common.NotifyChildFindingStateChangeInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.NotifyChildPublishingStateChangeInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.NotifyChildrenFindingStateChangeInteractor
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager

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
            @NotifyChildFindingStateChangeInteractorQualifier
            notifyChildFindingStateChangeInteractor: NotifyChildFindingStateChangeInteractor,
            @NotifyChildrenFindingStateChangeInteractorQualifier
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
