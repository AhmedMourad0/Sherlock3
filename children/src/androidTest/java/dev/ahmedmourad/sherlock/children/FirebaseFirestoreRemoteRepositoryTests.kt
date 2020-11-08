package dev.ahmedmourad.sherlock.children

import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import arrow.core.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dev.ahmedmourad.sherlock.children.fakes.FakeConnectivityManager
import dev.ahmedmourad.sherlock.children.fakes.FakePreferencesManager
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.children.remote.repositories.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.children.remote.utils.extractRetrievedChild
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.children.R
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.LogcatTree
import timber.log.Timber
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class FirebaseFirestoreRemoteRepositoryTests {

    private val simpleUser = { userId: UserId ->
        SimpleRetrievedUser.of(
                userId,
                DisplayName.of("Ahmed Mourad").orNull()!!,
                null
        )
    }

    private val childToPublish = { userId: UserId ->
        ChildToPublish.of(
                simpleUser(userId),
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
    }

    private lateinit var preferencesManager: FakePreferencesManager
    private lateinit var connectivityManager: FakeConnectivityManager

    private val auth by lazy {
        FirebaseAuth.getInstance().apply {
            useEmulator("192.168.43.20", 9099)
        }
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance().apply {
            this.useEmulator("192.168.43.20", 8080)
            this.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
        }
    }

    private lateinit var repo: FirebaseFirestoreRemoteRepository

    @Before
    fun setup() {

        Timber.plant(LogcatTree("Sherlock"))

        if (FirebaseApp.getApps(appCtx).isEmpty()) {
            FirebaseApp.initializeApp(
                    ApplicationProvider.getApplicationContext(),
                    FirebaseOptions.Builder()
                            .setApplicationId(appCtx.getString(R.string.firebase_application_id))
                            .setApiKey(appCtx.getString(R.string.firebase_api_key))
                            .setProjectId(appCtx.getString(R.string.firebase_project_id))
                            .setDatabaseUrl(appCtx.getString(R.string.firebase_database_url))
                            .setStorageBucket(appCtx.getString(R.string.firebase_storage_bucket))
                            .setGcmSenderId(appCtx.getString(R.string.firebase_gcm_sender_id))
                            .build()
            )
        }

        preferencesManager = FakePreferencesManager()
        connectivityManager = FakeConnectivityManager()

        repo = FirebaseFirestoreRemoteRepository(db = {
            firestore
        }, preferencesManager = {
            preferencesManager
        }, connectivityManager = {
            connectivityManager
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
        })
    }

    @Test
    fun publish_shouldAddTheChildToFirestoreAndReturnItAsRetrievedChild() {

        val uid = signInAnonymously()
        val cid = ChildId(UUID.randomUUID().toString())

        val childToPublish = childToPublish(uid)

        repo.publish(
                cid,
                childToPublish,
                null
        ).test().await().assertValue {
            childToPublish.matches(it.orNull()!!)
        }

        val retrievedEither = findById(cid) {
            if (it == uid) {
                simpleUser(uid)
            } else {
                null
            }
        }

        assertTrue(retrievedEither.isRight())
        retrievedEither as Either.Right
        assertNotNull(retrievedEither.b)
        assertTrue(childToPublish.matches(retrievedEither.b!!))
    }

    private fun findById(
            id: ChildId,
            findUser: (UserId) -> SimpleRetrievedUser?
    ): Either<RemoteRepository.FindException, RetrievedChild?> {

        return Single.create<Either<RemoteRepository.FindException, DocumentSnapshot?>> { emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onSuccess(
                            RemoteRepository.FindException.UnknownException(exception).left()
                    )

                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onSuccess(snapshot.right())
                    } else {
                        emitter.onSuccess(null.right())
                    }
                }
            }

            val registration = firestore.collection(Contract.Database.Children.PATH)
                    .document(id.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { either ->
                    either.flatMap { snapshot ->

                        snapshot ?: return@flatMap null.right()

                        val userId = snapshot.getString(Contract.Database.Children.USER_ID)
                                ?.let(::UserId)
                                ?: return@flatMap RemoteRepository.FindException.InternalException(
                                        ModelCreationException("Can't find user for child with id: ${id.value}")
                                ).left()

                        extractRetrievedChild(snapshot, findUser(userId)!!).mapLeft {
                            RemoteRepository.FindException.InternalException(it)
                        }
                    }
                }.blockingGet()
    }

    private fun signInAnonymously(): UserId {
        return Single.create<Either<Throwable, UserId>> { emitter ->
            auth.signInAnonymously().addOnSuccessListener {
                emitter.onSuccess(UserId(it.user!!.uid).right())
            }.addOnFailureListener {
                emitter.onSuccess(it.left())
            }
        }.blockingGet().orNull()!!
    }

    private fun ChildToPublish.matches(other: RetrievedChild): Boolean {
        return this.name == other.name &&
                this.user == other.user &&
                this.notes == other.notes &&
                this.location == other.location &&
                this.appearance == other.appearance
    }
}
