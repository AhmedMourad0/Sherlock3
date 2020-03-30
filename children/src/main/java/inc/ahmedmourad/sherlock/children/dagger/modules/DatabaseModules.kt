package inc.ahmedmourad.sherlock.children.dagger.modules

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseFirestoreQualifier
import inc.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseStorageQualifier
import inc.ahmedmourad.sherlock.children.local.database.SherlockDatabase

@Module
internal object SherlockDatabaseModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideSherlockDatabase(): SherlockDatabase = SherlockDatabase.getInstance()
}

@Module
internal object FirebaseFirestoreModule {
    @Provides
    @Reusable
    @ChildrenFirebaseFirestoreQualifier
    @JvmStatic
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
internal object FirebaseStorageModule {
    @Provides
    @Reusable
    @ChildrenFirebaseStorageQualifier
    @JvmStatic
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
