package dev.ahmedmourad.sherlock.children

import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import arrow.core.orNull
import arrow.core.right
import arrow.core.some
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dev.ahmedmourad.sherlock.children.fakes.FakeConnectivityManager
import dev.ahmedmourad.sherlock.children.fakes.FakePreferencesManager
import dev.ahmedmourad.sherlock.children.remote.repositories.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.LogcatTree
import timber.log.Timber
import timber.log.error
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4ClassRunner::class)
class FirebaseFirestoreRemoteRepositoryTests {

    private val childToPublish = ChildToPublish.of(
            SimpleRetrievedUser.of(
                    UserId(UUID.randomUUID().toString()),
                    DisplayName.of("Ahmed Mourad").orNull()!!,
                    null
            ),
            FullName.of(Name.of("Jack").orNull()!!, Name.of("McBigFeet").orNull()!!).right(),
            null,
            Location.of(null, null, null, Coordinates.of(77.0, 88.0).orNull()!!),
            ApproximateAppearance.of(
                    Gender.MALE,
                    Skin.WHEAT,
                    Hair.BROWN,
                    AgeRange.of(Age.of(11).orNull()!!, Age.of(17).orNull()!!).orNull()!!,
                    HeightRange.of(Height.of(70).orNull()!!, Height.of(120).orNull()!!).orNull()!!
            ).orNull()!!,
            null
    ).orNull()!!

    private lateinit var preferencesManager: FakePreferencesManager
    private lateinit var connectivityManager: FakeConnectivityManager

    private lateinit var repo: FirebaseFirestoreRemoteRepository

    @Before
    fun setup() {
//        RuntimeEnvironment.systemContext.injectAsAppCtx()
        Timber.plant(LogcatTree("Sherlock"))
        FirebaseApp.initializeApp(
                ApplicationProvider.getApplicationContext(),
                FirebaseOptions.Builder()
//                        .setDatabaseUrl("<VAL>")
//                        .setGcmSenderId("<VAL>")
                        .build()
        )
//        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        preferencesManager = FakePreferencesManager()
        connectivityManager = FakeConnectivityManager()
        repo = FirebaseFirestoreRemoteRepository(db = {
            FirebaseFirestore.getInstance().apply {
//                this.useEmulator("10.0.2.2", 8081)
                this.firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(false)
                        .build()
            }
        }, preferencesManager = {
            preferencesManager
        }, authStateObservable = {
            Flowable.just(true.right())
        }, simpleSignedInUserObservable = {
            Flowable.just(SimpleRetrievedUser.of(
                    UserId(UUID.randomUUID().toString()),
                    DisplayName.of("Ahmed Mourad").orNull()!!,
                    null
            ).some())
        }, findSimpleUsers = { ids ->
            Flowable.just(ids.map {
                SimpleRetrievedUser.of(
                        it,
                        DisplayName.of("Ahmed Mourad").orNull()!!,
                        null
                )
            }.right())
        }, connectivityManager = {
            connectivityManager
        })
    }

    @Test
    fun publish_shouldAddTheChildToTheRemoteRepoAndReturnItAsRetrievedChild() {
        val id = ChildId(UUID.randomUUID().toString())
        Timber.error {
            repo.publish(
                    id,
                    childToPublish,
                    null
            ).test().await(60, TimeUnit.SECONDS).toString()
        }
        Timber.error { repo.find(id).firstOrError().test().await().values().toString() }
    }
}
