package dev.ahmedmourad.sherlock.children.dagger.modules

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseFirestoreQualifier
import dev.ahmedmourad.sherlock.children.dagger.modules.qualifiers.ChildrenFirebaseStorageQualifier
import dev.ahmedmourad.sherlock.children.local.database.SherlockDatabase

@Module
internal object SherlockDatabaseModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): SherlockDatabase = SherlockDatabase.getInstance()
}

@Module
internal object FirebaseFirestoreModule {
    @Provides
    @Reusable
    @ChildrenFirebaseFirestoreQualifier
    @JvmStatic
    fun provide(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
internal object FirebaseStorageModule {
    @Provides
    @Reusable
    @ChildrenFirebaseStorageQualifier
    @JvmStatic
    fun provide(): FirebaseStorage = FirebaseStorage.getInstance()
}
