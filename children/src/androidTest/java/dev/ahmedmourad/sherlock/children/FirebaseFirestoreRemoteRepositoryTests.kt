package dev.ahmedmourad.sherlock.children

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import arrow.core.*
import arrow.core.extensions.fx
import arrow.core.extensions.list.foldable.all
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import dev.ahmedmourad.sherlock.children.fakes.*
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.children.remote.model.QueryId
import dev.ahmedmourad.sherlock.children.remote.repositories.FirebaseFirestoreRemoteRepository
import dev.ahmedmourad.sherlock.children.remote.utils.*
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.children.R
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.LogcatTree
import timber.log.Timber
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class FirebaseFirestoreRemoteRepositoryTests {

    private val simpleUserFactory = { userId: UserId ->
        SimpleRetrievedUser.of(
                userId,
                DisplayName.of("Ahmed Mourad").orNull()!!,
                null
        )
    }

    private val childToPublishFactory = { userId: UserId ->
        ChildToPublish.of(
                simpleUserFactory(userId),
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
    private val queryFactory = { page: Int, userId: UserId ->
        ChildrenQuery.of(
                simpleUserFactory(userId),
                FullName.of(Name.of("Jack").orNull()!!, Name.of("McBigFeet").orNull()!!),
                Coordinates.of((-90..90).random().toDouble(), (-180..180).random().toDouble()).orNull()!!,
                ExactAppearance.of(
                        Gender.MALE,
                        Skin.WHEAT,
                        Hair.BROWN,
                        Age.of((7..15).random()).orNull()!!,
                        Height.of((70..140).random()).orNull()!!
                ),
                page
        ).orNull()!!
    }

    private val investigationFactory = { userId: UserId -> queryFactory(0, userId).toInvestigation() }

    private lateinit var preferencesManager: FakePreferencesManager
    private lateinit var connectivityManager: FakeConnectivityManager
    private lateinit var authStateObservable: FakeObserveUserAuthState
    private lateinit var findSimpleUsers: FakeFindSimpleUsers
    private lateinit var simpleSignedInUserObservable: FakeObserveSimpleSignedInUser

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

        preferencesManager = FakePreferencesManager()
        connectivityManager = FakeConnectivityManager()
        authStateObservable = FakeObserveUserAuthState()
        findSimpleUsers = FakeFindSimpleUsers()
        simpleSignedInUserObservable = FakeObserveSimpleSignedInUser {
            simpleUserFactory(UserId(UUID.randomUUID().toString()))
        }

        repo = FirebaseFirestoreRemoteRepository(
                db = { firestore },
                preferencesManager = { preferencesManager },
                connectivityManager = { connectivityManager },
                authStateObservable = authStateObservable,
                findSimpleUsers = findSimpleUsers,
                simpleSignedInUserObservable = simpleSignedInUserObservable
        )

        auth.signOut()
        flushAll()
    }

    @Test
    fun publish_shouldAddTheChildToFirestoreAndReturnItAsRetrievedChildWhenTheUserIdBelongsToTheCurrentUser() {

        val (_, uid) = signInAnonymously()
        val cid = ChildId(UUID.randomUUID().toString())

        val childToPublish = childToPublishFactory(uid)

        repo.publish(
                cid,
                childToPublish,
                null
        ).test().await().assertValue {
            assertEquals(cid, it.orNull()!!.id)
            childToPublish.matches(it.orNull()!!)
        }

        val retrievedEither = findChildById(cid) {
            if (it == uid) {
                simpleUserFactory(uid)
            } else {
                null
            }
        }

        assertTrue(retrievedEither.isRight())
        retrievedEither as Either.Right
        assertNotNull(retrievedEither.b)
        assertEquals(cid, retrievedEither.b!!.id)
        assertTrue(childToPublish.matches(retrievedEither.b!!))
    }

    @Test
    fun publish_shouldFailWhenTheUserIdDoesNotBelongsToTheCurrentUser() {

        signInAnonymously()
        val cid = ChildId(UUID.randomUUID().toString())

        val childToPublish = childToPublishFactory(UserId(UUID.randomUUID().toString()))

        repo.publish(
                cid,
                childToPublish,
                null
        ).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun publish_shouldFailWhenThereIsNoSignedInUser() {

        val cid = ChildId(UUID.randomUUID().toString())

        val childToPublish = childToPublishFactory(UserId(UUID.randomUUID().toString()))

        repo.publish(
                cid,
                childToPublish,
                null
        ).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun publish_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublishFactory(UserId(UUID.randomUUID().toString())),
                    null
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.PublishException.NoSignedInUserException
            }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun publish_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublishFactory(UserId(UUID.randomUUID().toString())),
                    null
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.PublishException.NoInternetConnectionException
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
    fun publish_shouldPropagateTheUnknownException() {

        fun go() {
            repo.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublishFactory(UserId(UUID.randomUUID().toString())),
                    null
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is RemoteRepository.PublishException.UnknownException
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
    fun find_shouldReturnTheRetrievedChildOfTheGivenIdIfPresent() {

        val cid = ChildId(UUID.randomUUID().toString())
        val (_, uid) = signInAnonymously()
        val child = childToPublishFactory(uid)
        publishChild(cid, child, null).orNull()!!

        fun go() {
            repo.find(cid).firstOrError().test().await().assertValue {
                assertTrue(it.isRight())
                it as Either.Right
                assertNotNull(it.b)
                assertEquals(cid, it.orNull()!!.id)
                child.matches(it.b!!)
            }
        }

        go()

        auth.signOut()
        val otherUid = signInAnonymously()
        assertNotEquals(uid, otherUid)

        go()
    }

    @Test
    fun find_shouldReturnNullRetrievedChildIfTheGivenIdIsNotPresent() {

        val cid = ChildId(UUID.randomUUID().toString())
        signInAnonymously()

        repo.find(cid).firstOrError().test().await().assertValue {
            assertTrue(it.isRight())
            it as Either.Right
            it.b == null
        }
    }

    @Test
    fun find_shouldFailWhenThereIsNoSignedInUser() {
        repo.find(ChildId(UUID.randomUUID().toString()))
                .firstOrError()
                .test()
                .await()
                .assertValue(Either<RemoteRepository.FindException, RetrievedChild?>::isLeft)
    }

    @Test
    fun find_shouldPropagateTheNoSignedInUserException() {

        val cid = ChildId(UUID.randomUUID().toString())
        val (_, uid) = signInAnonymously()
        val child = childToPublishFactory(uid)
        publishChild(cid, child, null).orNull()!!

        fun go() {
            repo.find(cid)
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindException.NoSignedInUserException
                    }
        }

        findSimpleUsers.isUserSignedIn = true
        authStateObservable.isUserSignedIn = false
        go()

        findSimpleUsers.isUserSignedIn = false
        authStateObservable.isUserSignedIn = true
        go()

        findSimpleUsers.isUserSignedIn = false
        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun find_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.find(ChildId(UUID.randomUUID().toString()))
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindException.NoInternetConnectionException
                    }
        }

        connectivityManager.hasInternet = true
        authStateObservable.hasInternet = false
        findSimpleUsers.hasInternet = false
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = true
        findSimpleUsers.hasInternet = false
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = false
        findSimpleUsers.hasInternet = true
        go()

        connectivityManager.hasInternet = false
        authStateObservable.hasInternet = false
        findSimpleUsers.hasInternet = false
        go()
    }

    @Test
    fun find_shouldReturnInternalExceptionIfTheUserWasNotFound() {

        val cid = ChildId(UUID.randomUUID().toString())
        val (_, uid) = signInAnonymously()
        val child = childToPublishFactory(uid)
        publishChild(cid, child, null).orNull()!!

        fun go() {
            repo.find(cid)
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a is RemoteRepository.FindException.InternalException
                    }
        }

        findSimpleUsers.users.clear()
        go()
    }

    @Test
    fun find_shouldPropagateTheUnknownException() {

        fun go() {
            repo.find(ChildId(UUID.randomUUID().toString()))
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a is RemoteRepository.FindException.UnknownException
                    }
        }

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = false
        findSimpleUsers.triggerUnknownException = false
        go()

        connectivityManager.triggerUnknownException = false
        authStateObservable.triggerUnknownException = true
        findSimpleUsers.triggerUnknownException = false
        go()

        connectivityManager.triggerUnknownException = false
        authStateObservable.triggerUnknownException = false
        findSimpleUsers.triggerUnknownException = true
        go()

        connectivityManager.triggerUnknownException = true
        authStateObservable.triggerUnknownException = true
        findSimpleUsers.triggerUnknownException = true
        go()
    }

    @Test
    fun addInvestigation_shouldAddTheInvestigationToFirestoreAndReturnItWhenTheUserIdBelongsToTheCurrentUser() {

        val (_, uid) = signInAnonymously()

        val investigation = investigationFactory(uid)

        val emptyRetrievedEither = findAllInvestigations(simpleUserFactory(uid))
        emptyRetrievedEither as Either.Right
        assertNotNull(emptyRetrievedEither.b)
        assertEquals(0, emptyRetrievedEither.b.size)

        repo.addInvestigation(investigation)
                .test()
                .await()
                .assertValue {
                    it.orNull()!! == investigation
                }

        val retrievedEither = findAllInvestigations(simpleUserFactory(uid))

        assertTrue(retrievedEither.isRight())
        retrievedEither as Either.Right
        assertNotNull(retrievedEither.b)
        assertEquals(1, retrievedEither.b.size)
        assertTrue(investigation.matches(retrievedEither.b.first()))
    }

    @Test
    fun addInvestigation_shouldFailWhenTheUserIdDoesNotBelongsToTheCurrentUser() {

        signInAnonymously()

        val investigation = investigationFactory(UserId(UUID.randomUUID().toString()))

        repo.addInvestigation(investigation).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun addInvestigation_shouldFailWhenThereIsNoSignedInUser() {

        val investigation = investigationFactory(UserId(UUID.randomUUID().toString()))

        repo.addInvestigation(investigation).test().await().assertValue {
            it.isLeft()
        }
    }

    @Test
    fun addInvestigation_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.addInvestigation(
                    investigationFactory(UserId(UUID.randomUUID().toString()))
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.AddInvestigationException.NoSignedInUserException
            }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun addInvestigation_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.addInvestigation(
                    investigationFactory(UserId(UUID.randomUUID().toString()))
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.AddInvestigationException.NoInternetConnectionException
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
    fun addInvestigation_shouldPropagateTheUnknownException() {

        fun go() {
            repo.addInvestigation(
                    investigationFactory(UserId(UUID.randomUUID().toString()))
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is RemoteRepository.AddInvestigationException.UnknownException
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
    fun findAllInvestigations_shouldReturnAllTheOngoingInvestigationsOfTheCurrentUser() {

        fun go() {

            val (_, uid) = signInAnonymously()

            simpleSignedInUserObservable.userFactory = {
                simpleUserFactory(uid)
            }

            repo.findAllInvestigations().firstOrError().test().await().assertValue {
                assertTrue(it.isRight())
                it as Either.Right
                assertNotNull(it.b)
                it.b.isEmpty()
            }

            val investigations = List((6..13).random()) {
                investigationFactory(uid)
            }

            investigations.forEach(this::addInvestigation)

            repo.findAllInvestigations().firstOrError().test().await().assertValue { either ->
                assertTrue(either.isRight())
                either as Either.Right
                assertNotNull(either.b)
                assertEquals(investigations.size, either.b.size)
                either.b.all { retrieved -> investigations.any { retrieved.matches(it) } }
            }

            val investigationsExtra = List((1..6).random()) {
                investigationFactory(uid)
            }

            investigationsExtra.forEach(this::addInvestigation)

            val allInvestigations = investigations + investigationsExtra
            repo.findAllInvestigations().firstOrError().test().await().assertValue { either ->
                assertTrue(either.isRight())
                either as Either.Right
                assertNotNull(either.b)
                assertEquals(allInvestigations.size, either.b.size)
                either.b.all { retrieved -> allInvestigations.any { retrieved.matches(it) } }
            }

            auth.signOut()
        }

        repeat(2) {
            go()
        }
    }

    @Test
    fun findAllInvestigations_shouldFailWhenThereIsNoSignedInUser() {
        repo.findAllInvestigations()
                .firstOrError()
                .test()
                .await()
                .assertValue(Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>>::isLeft)
    }

    @Test
    fun findAllInvestigations_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.findAllInvestigations()
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindAllInvestigationsException.NoSignedInUserException
                    }
        }

        simpleSignedInUserObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun findAllInvestigations_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.findAllInvestigations()
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a == RemoteRepository.FindAllInvestigationsException.NoInternetConnectionException
                    }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun findAllInvestigations_shouldPropagateTheUnknownException() {

        fun go() {
            repo.findAllInvestigations()
                    .firstOrError()
                    .test()
                    .await()
                    .assertValue {
                        assertTrue(it is Either.Left)
                        it as Either.Left
                        it.a is RemoteRepository.FindAllInvestigationsException.UnknownException
                    }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun findAll_shouldAddTheQueryToFirestoreWhenTheUserIdBelongsToTheCurrentUser() {

        val (_, uid) = signInAnonymously()

        findChildrenQuery(simpleUserFactory(uid))
                .test()
                .await()
                .assertValue(Option<ChildrenQuery>::isEmpty)

        val query = queryFactory((0..5).random(), uid)

        repo.findAll(query)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        findChildrenQuery(simpleUserFactory(uid))
                .test()
                .await()
                .assertNoErrors()
                .assertValue {
                    assertTrue(it.isDefined())
                    it as Some
                    it.t == query
                }
    }

    @Test
    fun findAll_shouldReturnTheResultsOfTheQuery() {

        val (_, uid) = signInAnonymously()

        val query = queryFactory(0, uid)

        repo.findAll(query)
                .firstOrError()
                .test()
                .await()
                .assertValue {
                    assertTrue(it.isRight())
                    it as Either.Right
                    it.b.isEmpty()
                }

        val children: List<Triple<ChildId, ChildToPublish, Url?>> = List((0..19).random()) {
            Triple(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublishFactory(uid),
                    null
            )
        }

        children.forEach { (childId, child, url) ->
            publishChild(childId, child, url)
        }

        fun predicate(results: Map<SimpleRetrievedChild, Weight>): Boolean {
            return children.size == results.size && results.keys.all { retrieved ->
                children.any {
                    it.first == retrieved.id &&
                            it.second.matches(retrieved) &&
                            it.third == retrieved.pictureUrl
                }
            }
        }

        repo.findAll(query)
                .filter { either ->
                    either.orNull()?.let(::predicate) ?: false
                }.firstOrError()
                .test()
                .await()
                .assertValue { either ->
                    either.isRight() && predicate(either.orNull()!!)
                }
    }

    @Test
    fun findAll_shouldFailWhenTheUserIdDoesNotBelongsToTheCurrentUser() {

        signInAnonymously()

        val query = queryFactory((0..5).random(), UserId(UUID.randomUUID().toString()))

        repo.findAll(query)
                .firstOrError()
                .test()
                .await()
                .assertValue {
                    it.isLeft()
                }
    }

    @Test
    fun findAll_shouldFailWhenThereIsNoSignedInUser() {

        val query = queryFactory((0..5).random(), UserId(UUID.randomUUID().toString()))

        repo.findAll(query)
                .firstOrError()
                .test()
                .await()
                .assertValue {
                    it.isLeft()
                }
    }

    @Test
    fun findAll_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.findAll(
                    queryFactory(0, UserId(UUID.randomUUID().toString()))
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.FindAllException.NoSignedInUserException
            }
        }

        authStateObservable.isUserSignedIn = false
        go()
    }

    @Test
    fun findAll_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.findAll(
                    queryFactory(0, UserId(UUID.randomUUID().toString()))
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == RemoteRepository.FindAllException.NoInternetConnectionException
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
    fun findAll_shouldPropagateTheUnknownException() {

        fun go() {
            repo.findAll(
                    queryFactory(0, UserId(UUID.randomUUID().toString()))
            ).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is RemoteRepository.FindAllException.UnknownException
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
    fun invalidateAllQueries_shouldDeleteTheQueryOfTheCurrentSignedInUser() {

        val (_, uid) = signInAnonymously()

        findChildrenQuery(simpleUserFactory(uid))
                .test()
                .await()
                .assertValue(Option<ChildrenQuery>::isEmpty)

        val query = queryFactory((0..5).random(), uid)

        repo.findAll(query)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        findChildrenQuery(simpleUserFactory(uid))
                .test()
                .await()
                .assertNoErrors()
                .assertValue(Option<ChildrenQuery>::isDefined)

        repo.invalidateAllQueries()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        findChildrenQuery(simpleUserFactory(uid))
                .test()
                .await()
                .assertValue(Option<ChildrenQuery>::isEmpty)
    }

    @Test
    fun invalidateAllQueries_shouldNotFailWhenThereIsNoSignedInUser() {

        repo.invalidateAllQueries()
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        val (_, uid) = signInAnonymously()

        repo.invalidateAllQueries()
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        val query = queryFactory((0..5).random(), uid)

        addChildQuery(query).test()
                .await()
                .assertComplete()
                .assertNoErrors()
                .assertValue(Either<RemoteRepository.FindAllException, QueryId>::isRight)

        auth.signOut()

        repo.invalidateAllQueries()
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()
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

    private fun findChildById(
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

        }.map { either ->
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

    private fun findChildrenQuery(user: SimpleRetrievedUser): Single<Option<ChildrenQuery>> {

        return Single.create { emitter ->

            val id = preferencesManager.getDeviceId()

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onError(exception)

                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        extractChildQuery(user, snapshot).fold(ifLeft = {
                            emitter.onError(it)
                        }, ifRight = {
                            emitter.onSuccess(it.some())
                        })
                    } else {
                        emitter.onSuccess(none())
                    }
                }
            }

            val registration = firestore.collection(Contract.Database.Queries.PATH)
                    .document(id)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }
        }
    }

    private fun findAllInvestigations(
            user: SimpleRetrievedUser
    ): Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>> {

        return Single.create<Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>>> { emitter ->

            val snapshotListener = { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onSuccess(
                            RemoteRepository.FindAllInvestigationsException.UnknownException(exception).left()
                    )

                } else if (snapshot != null) {

                    emitter.onSuccess(
                            snapshot.documents
                                    .filter(DocumentSnapshot::exists)
                                    .mapNotNull { extractChildInvestigation(user, it).orNull() }
                                    .distinct()
                                    .right()
                    )
                }
            }

            val registration = firestore.collection(Contract.Database.Investigations.PATH)
                    .whereEqualTo(Contract.Database.Investigations.USER_ID, user.id.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }.blockingGet()
    }

    private fun publishChild(
            childId: ChildId,
            child: ChildToPublish,
            pictureUrl: Url?
    ): Either<RemoteRepository.PublishException, RetrievedChild> {
        return Single.create<Either<RemoteRepository.PublishException, RetrievedChild>> { emitter ->

            val successListener = { _: Void? ->
                findSimpleUsers.users.add(child.user)
                emitter.onSuccess(child.toRetrievedChild(
                        childId,
                        System.currentTimeMillis(),
                        pictureUrl
                ).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.PublishException.UnknownException(throwable).left()
                )
            }

            firestore.collection(Contract.Database.Children.PATH)
                    .document(childId.value)
                    .set(child.toMap(pictureUrl))
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.blockingGet()
    }

    private fun addInvestigation(
            investigation: Investigation
    ): Either<RemoteRepository.AddInvestigationException, Investigation> {
        return Single.create<Either<RemoteRepository.AddInvestigationException, Investigation>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(investigation.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.AddInvestigationException.UnknownException(throwable).left()
                )
            }

            firestore.collection(Contract.Database.Investigations.PATH)
                    .document(UUID.randomUUID().toString())
                    .set(investigation.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.blockingGet()
    }

    private fun addChildQuery(
            query: ChildrenQuery
    ): Single<Either<RemoteRepository.FindAllException, QueryId>> {
        return Single.create<Either<RemoteRepository.FindAllException, QueryId>> { emitter ->

            val id = preferencesManager.getDeviceId()

            val successListener = { _: Void? ->
                emitter.onSuccess(QueryId(id).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(
                        RemoteRepository.FindAllException.UnknownException(throwable).left()
                )
            }

            firestore.collection(Contract.Database.Queries.PATH)
                    .document(id)
                    .set(query.toMap())
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
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
            preferencesManager.invalidateDeviceId()
        }.blockingGet().orNull()!!
    }
}

private fun Investigation.matches(other: Investigation): Boolean {
    return this.user == other.user &&
            this.fullName == other.fullName &&
            this.coordinates == other.coordinates &&
            this.appearance == other.appearance
}

private fun ChildToPublish.matches(other: RetrievedChild): Boolean {
    return this.name == other.name &&
            this.user == other.user &&
            this.notes == other.notes &&
            this.location == other.location &&
            this.appearance == other.appearance
}

private fun ChildToPublish.matches(other: SimpleRetrievedChild): Boolean {
    return this.name == other.name &&
            this.user == other.user &&
            this.notes == other.notes &&
            this.location?.address == other.locationAddress &&
            this.location?.name == other.locationName
}

private data class FirebaseToken(val value: String)

internal fun extractChildQuery(
        user: SimpleRetrievedUser,
        snapshot: DocumentSnapshot
): Either<ModelCreationException, ChildrenQuery> {

    val id = snapshot.id

    return Either.fx {

        val firstName = snapshot.getString(Contract.Database.Queries.FIRST_NAME)
                ?.let(Name.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("first name is null for id=$id").left()

        val lastName = snapshot.getString(Contract.Database.Queries.LAST_NAME)
                ?.let(Name.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("last name is null for id=$id").left()

        val fullName = FullName.of(firstName.bind(), lastName.bind())

        val coordinates = extractCoordinates(
                snapshot,
                Contract.Database.Queries.LATITUDE,
                Contract.Database.Queries.LONGITUDE
        ).bind()?.right() ?: ModelCreationException("last name is null for id=$id").left()

        val appearance = extractExactAppearance(snapshot, id).bind()

        val page = snapshot.getLong(Contract.Database.Queries.PAGE)?.toInt()?.right()
                ?: ModelCreationException("page is null for id=$id").left()

        ChildrenQuery.of(
                user,
                fullName,
                coordinates.bind(),
                appearance,
                page.bind()
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}
