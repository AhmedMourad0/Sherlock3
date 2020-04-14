package dev.ahmedmourad.sherlock.auth.dagger.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.AuthFirebaseFirestoreQualifier
import dev.ahmedmourad.sherlock.auth.dagger.modules.qualifiers.AuthFirebaseStorageQualifier

@Module
internal object FirebaseFirestoreModule {
    @Provides
    @Reusable
    @AuthFirebaseFirestoreQualifier
    @JvmStatic
    fun provide(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
internal object FirebaseStorageModule {
    @Provides
    @Reusable
    @AuthFirebaseStorageQualifier
    @JvmStatic
    fun provide(): FirebaseStorage = FirebaseStorage.getInstance()
}

@Module
internal object FirebaseAuthModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): FirebaseAuth = FirebaseAuth.getInstance()
}
