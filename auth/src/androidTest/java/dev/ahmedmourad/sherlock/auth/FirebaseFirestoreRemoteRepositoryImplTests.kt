package dev.ahmedmourad.sherlock.auth

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import arrow.core.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import dev.ahmedmourad.sherlock.auth.fakes.FakeConnectivityManager
import dev.ahmedmourad.sherlock.auth.fakes.FakeObserveUserAuthState
import dev.ahmedmourad.sherlock.auth.manager.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.auth.remote.contract.Contract
import dev.ahmedmourad.sherlock.auth.remote.repository.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.auth.remote.repository.extractSignedInUser
import dev.ahmedmourad.sherlock.auth.remote.utils.toMap
import dev.ahmedmourad.sherlock.auth.utils.*
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.auth.R
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.LogcatTree
import timber.log.Timber
import java.util.*
import kotlin.random.Random

@RunWith(AndroidJUnit4ClassRunner::class)
class FirebaseFirestoreRemoteRepositoryImplTests {

    private lateinit var connectivityManager: FakeConnectivityManager
    private lateinit var authStateObservable: FakeObserveUserAuthState

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var repo: FirebaseFirestoreRemoteRepository

    @Before
    fun setup() {

        Timber.plant(LogcatTree("Sherlock"))

        val initializeFirebase = FirebaseApp.getApps(appCtx).isEmpty()

        if (initializeFirebase) {
            FirebaseApp.initializeApp(appCtx,
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

        auth = FirebaseAuth.getInstance().apply {
            if (initializeFirebase) {
                useEmulator(appCtx.getString(R.string.local_ip_address), 9099)
            }
        }

        firestore = FirebaseFirestore.getInstance().apply {
            if (initializeFirebase) {
                this.useEmulator(appCtx.getString(R.string.local_ip_address), 8080)
            }
            this.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
        }

        connectivityManager = FakeConnectivityManager()
        authStateObservable = FakeObserveUserAuthState()

        repo = FirebaseFirestoreRemoteRepository(
                db = { firestore },
                connectivityManager = { connectivityManager },
                authStateObservable = authStateObservable
        )

        auth.signOut()
        flushAll()
    }

    @Test
    fun storeSignUpUser_shouldStoreTheUserDataAndReturnSignedInUser() {

        val (_, uid) = signInAnonymously()

        val user = remoteSignUpUser(uid, randomEmail())

        repo.storeSignUpUser(user).test().await().assertValue {
            assertEquals(uid, it.orNull()!!.id)
            user.matches(it.orNull()!!)
        }

        val retrievedEither = findSignedInUser(uid)

        assertTrue(retrievedEither.isRight())
        retrievedEither as Either.Right
        assertNotNull(retrievedEither.b)
        assertEquals(uid, retrievedEither.b!!.id)
        assertTrue(user.matches(retrievedEither.b!!))
    }

    @Test
    fun storeSignUpUser_shouldFailWhenTheUserIdDoesNotBelongsToTheCurrentUser() {

        signInAnonymously()

        val user = remoteSignUpUser(UserId(UUID.randomUUID().toString()), randomEmail())

        repo.storeSignUpUser(user).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun storeSignUpUser_shouldFailWhenThereIsNoSignedInUser() {

        val user = remoteSignUpUser(UserId(UUID.randomUUID().toString()), randomEmail())

        repo.storeSignUpUser(user).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun storeSignUpUser_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            val user = remoteSignUpUser(UserId(UUID.randomUUID().toString()), randomEmail())
            repo.storeSignUpUser(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.StoreSignUpUserException.NoSignedInUserException
            }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun storeSignUpUser_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            val user = remoteSignUpUser(UserId(UUID.randomUUID().toString()), randomEmail())
            repo.storeSignUpUser(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.StoreSignUpUserException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = true
        authStateObservable.hasInternet = false
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = true
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = false
        go()
    }

    @Test
    fun storeSignUpUser_shouldPropagateTheUnknownException() {

        fun go() {
            val user = remoteSignUpUser(UserId(UUID.randomUUID().toString()), randomEmail())
            repo.storeSignUpUser(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is RemoteRepository.StoreSignUpUserException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = false
        go()

        connectivityManager.triggerUnknownException = false
        authStateObservable.triggerUnknownException = true
        go()

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = true
        go()
    }

    @Test
    fun findSignedInUser_shouldReturnTheSignedInUserOfTheGivenIdIfPresent() {

        val (_, uid) = signInAnonymously()
        val user = remoteSignUpUser(uid, randomEmail())

        fun go() {
            repo.findSignedInUser(uid).firstOrError().test().await().assertValue {
                assertTrue(it.isRight())
                it as Either.Right
                assertNotNull(it.b)
                assertEquals(uid, it.orNull()!!.id)
                user.matches(it.b!!)
            }
        }

        storeSignUpUser(user).orNull()!!
        go()

        auth.signOut()
        val otherUid = signInAnonymously()
        assertNotEquals(uid, otherUid)

        go()
    }

    @Test
    fun findSignedInUser_shouldReturnNullRetrievedChildIfTheGivenIdIsNotPresent() {

        val (_, uid) = signInAnonymously()

        repo.findSignedInUser(uid).firstOrError().test().await().assertValue {
            assertTrue(it.isRight())
            it as Either.Right
            it.b == null
        }
    }


    @Test
    fun findSignedInUser_shouldFailWhenThereIsNoSignedInUser() {
        repo.findSignedInUser(UserId(UUID.randomUUID().toString()))
                .firstOrError()
                .test()
                .await()
                .assertValue(Either<RemoteRepository.FindSignedInUserException, SignedInUser?>::isLeft)
    }

    @Test
    fun findSignedInUser_shouldPropagateTheNoSignedInUserException() {

        val (_, uid) = signInAnonymously()
        val user = remoteSignUpUser(uid, randomEmail())
        storeSignUpUser(user).orNull()!!

        fun go() {
            repo.findSignedInUser(uid)
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindSignedInUserException.NoSignedInUserException
                    }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun findSignedInUser_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.findSignedInUser(UserId(UUID.randomUUID().toString()))
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindSignedInUserException.NoInternetConnectionException
                    }
        }

        connectivityManager.hasInternet = true
        authStateObservable.hasInternet = false
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = true
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = false
        go()
    }

    @Test
    fun findSignedInUser_shouldPropagateTheUnknownException() {

        fun go() {
            repo.findSignedInUser(UserId(UUID.randomUUID().toString()))
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a is RemoteRepository.FindSignedInUserException.UnknownException
                    }
        }

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = false
        go()

        connectivityManager.triggerUnknownException = false
        authStateObservable.triggerUnknownException = true
        go()

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = true
        go()
    }

    @Test
    fun findSimpleUsers_shouldReturnTheSimpleRetrievedUsersOfTheGivenIds() {

        signInAnonymously()

        repo.findSimpleUsers(List((0..15).random()) {
            UserId(UUID.randomUUID().toString())
        }).firstOrError()
                .test()
                .await()
                .assertValue {
                    assertTrue(it.isRight())
                    it as Either.Right
                    it.b.isEmpty()
                }

        val users = List((7..15).random()) {
            auth.signOut()
            val (_, uid) = signInAnonymously()
            remoteSignUpUser(uid, randomEmail()).also(::storeSignUpUser)
        }

        if (Random.nextBoolean()) {
            signInAnonymously()
        }

        val usersToFetch = users.shuffled().take((1..users.size).random())

        repo.findSimpleUsers(usersToFetch.map { it.id })
                .firstOrError()
                .test()
                .await()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    assertEquals(usersToFetch.size, either.b.size)
                    either.b.all { retrieved -> usersToFetch.any { it.matches(retrieved) } }
                }
    }

    @Test
    fun findSimpleUsers_shouldFailWhenThereIsNoSignedInUser() {

        repo.findSimpleUsers(List((0..15).random()) { UserId(UUID.randomUUID().toString()) })
                .firstOrError()
                .test()
                .await()
                .assertValue {
                    it.isLeft()
                }
    }

    @Test
    fun findSimpleUsers_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.findSimpleUsers(List((0..15).random()) { UserId(UUID.randomUUID().toString()) })
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindSimpleUsersException.NoSignedInUserException
                    }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun findSimpleUsers_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.findSimpleUsers(List((0..15).random()) { UserId(UUID.randomUUID().toString()) })
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindSimpleUsersException.NoInternetConnectionException
                    }
        }

        connectivityManager.hasInternet = true
        authStateObservable.hasInternet = false
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = true
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = false
        go()
    }

    @Test
    fun findSimpleUsers_shouldPropagateTheUnknownException() {

        fun go() {
            repo.findSimpleUsers(List((0..15).random()) { UserId(UUID.randomUUID().toString()) })
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a is RemoteRepository.FindSimpleUsersException.UnknownException
                    }
        }

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = false
        go()

        connectivityManager.triggerUnknownException = false
        authStateObservable.triggerUnknownException = true
        go()

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = true
        go()
    }

    @Test
    fun updateUserLastLoginDate_shouldUpdateTheLastLoginDateOfTheUserOfTheGivenIdToNow() {

        val (_, uid) = signInAnonymously()
        assertTrue(storeSignUpUser(remoteSignUpUser(uid, randomEmail())).isRight())

        repo.updateUserLastLoginDate(uid)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()
                .assertValue(Unit.right())

        val lastLogin = findLastLoginDate(uid)!!
        assertTrue(System.currentTimeMillis() - lastLogin < 2000L)

        repo.updateUserLastLoginDate(uid)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()
                .assertValue(Unit.right())

        val lastLoginLater = findLastLoginDate(uid)!!
        assertTrue(lastLoginLater > lastLogin)
        assertTrue(System.currentTimeMillis() - lastLoginLater < 2000L)
    }

    @Test
    fun updateUserLastLoginDate_shouldFailWhenTheUserIdDoesNotBelongsToTheCurrentUser() {
        signInAnonymously()
        repo.updateUserLastLoginDate(UserId(UUID.randomUUID().toString())).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun updateUserLastLoginDate_shouldFailWhenThereIsNoSignedInUser() {
        repo.updateUserLastLoginDate(UserId(UUID.randomUUID().toString())).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun updateUserLastLoginDate_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.updateUserLastLoginDate(UserId(UUID.randomUUID().toString())).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.UpdateUserLastLoginDateException.NoSignedInUserException
            }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun updateUserLastLoginDate_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.updateUserLastLoginDate(UserId(UUID.randomUUID().toString())).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.UpdateUserLastLoginDateException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = true
        authStateObservable.hasInternet = false
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = true
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = false
        go()
    }

    @Test
    fun updateUserLastLoginDate_shouldPropagateTheUnknownException() {

        fun go() {
            repo.updateUserLastLoginDate(UserId(UUID.randomUUID().toString())).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is RemoteRepository.UpdateUserLastLoginDateException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = false
        go()

        connectivityManager.triggerUnknownException = false
        authStateObservable.triggerUnknownException = true
        go()

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = true
        go()
    }

    private fun flushAll() {
        val projectId = appCtx.getString(R.string.firebase_project_id)
        val ipAddress = appCtx.getString(R.string.local_ip_address)
        flush("http://$ipAddress:8080/emulator/v1/projects/$projectId/databases/(default)/documents")
        val (token, _) = signInAnonymously()
        flush(url = "http://$ipAddress:9099/emulator/v1/projects/$projectId/accounts",
                headers = mapOf("Authorization" to "Bearer ${token.value}")
        )
        auth.signOut()
    }

    private fun flush(url: String, headers: Map<String, String>? = null) {

        val request = Request.Builder()
                .url(url)
                .apply {
                    headers?.forEach { (name, value) ->
                        this.addHeader(name, value)
                    }
                }.delete()
                .build()

        OkHttpClient().newCall(request).execute()
    }

    private fun signInAnonymously(): Pair<FirebaseToken, UserId> {
        return Single.create<Either<Throwable, Pair<FirebaseToken, UserId>>> { emitter ->
            auth.signInAnonymously().onSuccessTask { result ->
                result!!.user!!.getIdToken(true).continueWith {
                    FirebaseToken(it.result!!.token!!) to UserId(result.user!!.uid)
                }
            }.addOnSuccessListener {
                emitter.onSuccess(it.right())
            }.addOnFailureListener {
                emitter.onSuccess(it.left())
            }
        }.blockingGet().orNull()!!
    }

    private fun storeSignUpUser(
            user: RemoteSignUpUser
    ): Either<RemoteRepository.StoreSignUpUserException, SignedInUser> {

        return Single.create<Either<RemoteRepository.StoreSignUpUserException, SignedInUser>> { emitter ->

            val timestamp = System.currentTimeMillis()
            val successListener = { _: Void? ->
                emitter.onSuccess(user.toSignedInUser(timestamp).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.StoreSignUpUserException.UnknownException(throwable).left()
                )
            }

            firestore.collection(Contract.Database.Users.PATH)
                    .document(user.id.value)
                    .set(user.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.blockingGet()
    }

    private fun findSignedInUser(
            id: UserId
    ): Either<RemoteRepository.FindSignedInUserException, SignedInUser?> {

        return Single.create<Either<RemoteRepository.FindSignedInUserException, SignedInUser?>> { emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {
                    emitter.onSuccess(
                            RemoteRepository.FindSignedInUserException.UnknownException(exception).left()
                    )
                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onSuccess(extractSignedInUser(snapshot).mapLeft {
                            RemoteRepository.FindSignedInUserException.InternalException(
                                    ModelCreationException(it.toString())
                            )
                        })
                    } else {
                        emitter.onSuccess(null.right())
                    }
                }
            }

            val registration = firestore.collection(Contract.Database.Users.PATH)
                    .document(id.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }.blockingGet()
    }

    private fun findLastLoginDate(
            id: UserId
    ): Long? {

        return Single.create<Option<Long>> { emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {
                    emitter.onError(exception)
                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onSuccess(snapshot.getLong(Contract.Database.Users.LAST_LOGIN_TIMESTAMP).toOption())
                    } else {
                        emitter.onSuccess(none())
                    }
                }
            }

            val registration = firestore.collection(Contract.Database.Users.PATH)
                    .document(id.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }.blockingGet().orNull()
    }
}

private fun RemoteSignUpUser.matches(signedIn: SignedInUser): Boolean {
    return signedIn.id == this.id &&
            signedIn.email == this.email &&
            signedIn.username == this.username &&
            signedIn.displayName == this.displayName &&
            signedIn.phoneNumber == this.phoneNumber &&
            signedIn.pictureUrl == this.pictureUrl
}

private fun RemoteSignUpUser.matches(retrieved: SimpleRetrievedUser): Boolean {
    return retrieved.id == this.id &&
            retrieved.displayName == this.displayName &&
            retrieved.pictureUrl == this.pictureUrl
}

private data class FirebaseToken(val value: String)
