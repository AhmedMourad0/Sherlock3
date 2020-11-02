package dev.ahmedmourad.sherlock.children

import arrow.core.Either
import arrow.core.orNull
import arrow.core.right
import dev.ahmedmourad.sherlock.children.fakes.FakeBus
import dev.ahmedmourad.sherlock.children.fakes.FakeImageRepository
import dev.ahmedmourad.sherlock.children.fakes.FakeLocalRepository
import dev.ahmedmourad.sherlock.children.fakes.FakeRemoteRepository
import dev.ahmedmourad.sherlock.children.repository.ChildrenRepositoryImpl
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ChildrenRepositoryImplUnitTests {

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

    private val queryFactory = { page: Int ->
        ChildrenQuery.of(
                SimpleRetrievedUser.of(
                        UserId(UUID.randomUUID().toString()),
                        DisplayName.of("Ahmed Mourad").orNull()!!,
                        null
                ),
                FullName.of(Name.of("Jack").orNull()!!, Name.of("McBigFeet").orNull()!!),
                Coordinates.of(77.0, 88.0).orNull()!!,
                ExactAppearance.of(
                        Gender.MALE,
                        Skin.WHEAT,
                        Hair.BROWN,
                        Age.of(14).orNull()!!,
                        Height.of(100).orNull()!!
                ),
                page
        ).orNull()!!
    }

    private val investigation = queryFactory(0).toInvestigation()

    private lateinit var localRepository: FakeLocalRepository
    private lateinit var remoteRepository: FakeRemoteRepository
    private lateinit var imageRepository: FakeImageRepository
    private lateinit var bus: FakeBus

    private lateinit var repo: ChildrenRepositoryImpl

    @Before
    fun setup() {
        localRepository = FakeLocalRepository()
        remoteRepository = FakeRemoteRepository()
        imageRepository = FakeImageRepository()
        bus = FakeBus()
        repo = ChildrenRepositoryImpl(
                { localRepository },
                { remoteRepository },
                { imageRepository },
                { bus }
        )
    }

    @Test
    fun `publish should add the child to the remote repo and return it as a retrieved child`() {
        repo.publish(childToPublish).test().await().assertValue {
            val result = it.orNull()!!
            val retrieved = childToPublish.toRetrievedChild(result.id, result.timestamp, result.pictureUrl)
            assertTrue(remoteRepository.allChildren().contains(retrieved))
            assertEquals(1, remoteRepository.allChildren().size)
            childToPublish.matches(result)
        }
    }

    @Test
    fun `publish should propagate the NoSignedInUserException`() {

        fun go() {
            repo.publish(childToPublish).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.PublishException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        imageRepository.isUserSignedIn = true
        go()

        remoteRepository.isUserSignedIn = true
        imageRepository.isUserSignedIn = false
        go()

        remoteRepository.isUserSignedIn = false
        imageRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun `publish should propagate the NoInternetConnectionException`() {

        fun go() {
            repo.publish(childToPublish).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.PublishException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        imageRepository.hasInternet = true
        go()

        remoteRepository.hasInternet = true
        imageRepository.hasInternet = false
        go()

        remoteRepository.hasInternet = false
        imageRepository.hasInternet = false
        go()
    }

    @Test
    fun `publish should propagate the InternalException`() {

        fun go() {
            repo.publish(childToPublish).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.PublishException.InternalException
            }
        }

        imageRepository.triggerInternalException = true
        go()
    }

    @Test
    fun `publish should propagate the UnknownException`() {

        fun go() {
            repo.publish(childToPublish).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.PublishException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        imageRepository.triggerUnknownException = false
        go()

        remoteRepository.triggerUnknownException = false
        imageRepository.triggerUnknownException = true
        go()

        remoteRepository.triggerUnknownException = true
        imageRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun `find should return the child without weight when it exists in the remote repo only`() {

        val childId = ChildId(UUID.randomUUID().toString())

        remoteRepository.publish(
                childId,
                childToPublish,
                null
        ).test().await()

        repo.find(childId).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            assertNull(result!!.b)
            assertEquals(childId, result.a.id)
            childToPublish.matches(result.a)
        }
    }

    @Test
    fun `find should return the child with weight when it exists in the remote and local repos`() {

        val childId = ChildId(UUID.randomUUID().toString())

        remoteRepository.publish(
                childId,
                childToPublish,
                null
        ).test().await()

        localRepository.replaceAll(
                mapOf(childToPublish.toRetrievedChild(
                        childId,
                        System.currentTimeMillis(),
                        null
                ).simplify() to Weight.of(0.78).orNull()!!)
        ).test().await()

        repo.find(childId).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            assertNotNull(result!!.b)
            assertEquals(childId, result.a.id)
            childToPublish.matches(result.a)
        }
    }

    @Test
    fun `find should return null when the child exists in the local repo only`() {

        val childId = ChildId(UUID.randomUUID().toString())

        localRepository.replaceAll(
                mapOf(childToPublish.toRetrievedChild(
                        childId,
                        System.currentTimeMillis(),
                        null
                ).simplify() to Weight.of(0.78).orNull()!!)
        ).test().await()

        repo.find(childId).test().await().assertValue {
            assertTrue(it is Either.Right)
            val result = it.orNull()
            result == null
        }
    }

    @Test
    fun `find should return null when the child doesn't exist`() {

        val childId = ChildId(UUID.randomUUID().toString())

        repo.find(childId).test().await().assertValue {
            assertTrue(it is Either.Right)
            val result = it.orNull()
            result == null
        }
    }

    @Test
    fun `find should propagate the NoSignedInUserException`() {

        fun go() {
            repo.find(ChildId(UUID.randomUUID().toString())).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.FindException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun `find should propagate the NoInternetConnectionException`() {

        fun go() {
            repo.find(ChildId(UUID.randomUUID().toString())).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.FindException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun `find should propagate the InternalException`() {

        val childId = ChildId(UUID.randomUUID().toString())

        remoteRepository.publish(
                childId,
                childToPublish,
                null
        ).test().await()

        fun go() {
            repo.find(childId).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        localRepository.triggerInternalException = false
        go()

        remoteRepository.triggerInternalException = false
        localRepository.triggerInternalException = true
        go()

        remoteRepository.triggerInternalException = true
        localRepository.triggerInternalException = true
        go()
    }

    @Test
    fun `find should propagate the UnknownException`() {

        val childId = ChildId(UUID.randomUUID().toString())

        remoteRepository.publish(
                childId,
                childToPublish,
                null
        ).test().await()

        fun go() {
            repo.find(childId).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        localRepository.triggerUnknownException = false
        go()

        remoteRepository.triggerUnknownException = false
        localRepository.triggerUnknownException = true
        go()

        remoteRepository.triggerUnknownException = true
        localRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun `findAll should return a page size of children at max`() {

        Single.defer {
            remoteRepository.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublish,
                    null
            )
        }.repeat(10).test().await()

        repo.findAll(queryFactory(0)).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.size == 10
        }

        Single.defer {
            remoteRepository.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublish,
                    null
            )
        }.repeat(20).test().await()

        repo.findAll(queryFactory(0)).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.size == 20
        }

        repo.findAll(queryFactory(1)).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.size == 10
        }
    }

    @Test
    fun `findAll should add the query to the remote repo`() {

        assertEquals(0, remoteRepository.allQueries().size)

        val query = queryFactory(0)

        repo.findAll(query).test().await()

        assertEquals(1, remoteRepository.allQueries().size)

        assertEquals(query, remoteRepository.allQueries()[0])
    }

    @Test
    fun `findAll should replace the local db contents with its results`() {

        assertEquals(0, localRepository.allResults().size)

        Single.defer {
            remoteRepository.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublish,
                    null
            )
        }.repeat(15).test().await()

        repo.findAll(queryFactory(0)).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.size == 15
        }

        assertEquals(15, localRepository.allResults().size)
    }

    @Test
    fun `findAll should return an empty map when there are no results`() {

        repo.findAll(queryFactory(0)).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.isEmpty()
        }

        assertEquals(0, localRepository.allResults().size)
    }

    @Test
    fun `findAll should propagate the NoSignedInUserException`() {

        fun go() {
            repo.findAll(queryFactory(0)).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.FindAllException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun `findAll should propagate the NoInternetConnectionException`() {

        fun go() {
            repo.findAll(queryFactory(0)).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.FindAllException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun `findAll should propagate the InternalException`() {

        fun go() {
            repo.findAll(queryFactory(0)).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindAllException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun `findAll should propagate the UnknownException`() {

        fun go() {
            repo.findAll(queryFactory(0)).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindAllException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        localRepository.triggerUnknownException = false
        go()

        remoteRepository.triggerUnknownException = false
        localRepository.triggerUnknownException = true
        go()

        remoteRepository.triggerUnknownException = true
        localRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun `invalidateAllQueries should clear all queries from the remote repo`() {

        assertEquals(0, remoteRepository.allQueries().size)

        repo.findAll(queryFactory(0)).test().await()

        assertEquals(1, remoteRepository.allQueries().size)

        repo.invalidateAllQueries().test().await()

        assertEquals(0, remoteRepository.allQueries().size)
    }

    @Test
    fun `addInvestigation should add the investigation to the remote repo and return it`() {
        repo.addInvestigation(investigation).test().await().assertValue {
            val result = it.orNull()!!
            assertTrue(remoteRepository.allInvestigations().contains(investigation))
            assertEquals(1, remoteRepository.allInvestigations().size)
            result == investigation
        }
    }

    @Test
    fun `addInvestigation should propagate the NoSignedInUserException`() {

        fun go() {
            repo.addInvestigation(investigation).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.AddInvestigationException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun `addInvestigation should propagate the NoInternetConnectionException`() {

        fun go() {
            repo.addInvestigation(investigation).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.AddInvestigationException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun `addInvestigation should propagate the UnknownException`() {

        fun go() {
            repo.addInvestigation(investigation).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.AddInvestigationException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun `findAllInvestigations should return all ongoing investigations`() {

        Single.defer {
            remoteRepository.addInvestigation(investigation)
        }.repeat(10).test().await()

        repo.findAllInvestigations().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.size == 10
        }
    }

    @Test
    fun `findAllInvestigations should return an empty list when there are no ongoing investigations`() {
        repo.findAllInvestigations().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.isEmpty()
        }
    }

    @Test
    fun `findAllInvestigations should propagate the NoSignedInUserException`() {

        fun go() {
            repo.findAllInvestigations().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.FindAllInvestigationsException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun `findAllInvestigations should propagate the NoInternetConnectionException`() {

        fun go() {
            repo.findAllInvestigations().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.FindAllInvestigationsException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun `findAllInvestigations should propagate the InternalException`() {

        fun go() {
            repo.findAllInvestigations().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindAllInvestigationsException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun `findAllInvestigations should propagate the UnknownException`() {

        fun go() {
            repo.findAllInvestigations().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindAllInvestigationsException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun `findLastSearchResults should return all results in the local repo that contain a weight`() {

        val items = mutableMapOf<SimpleRetrievedChild, Weight>()
        repeat(10) {
            items[childToPublish.toRetrievedChild(
                    ChildId(UUID.randomUUID().toString()),
                    System.currentTimeMillis(),
                    null
            ).simplify()] = Weight.of((700..1000).random() / 1000.0).orNull()!!
        }

        localRepository.replaceAll(items).test().await()

        repo.findLastSearchResults().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            items == result
        }
    }

    @Test
    fun `findLastSearchResults should return an empty map when there are no results`() {

        repo.findLastSearchResults().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.isEmpty()
        }

        assertEquals(0, localRepository.allResults().size)
    }

    @Test
    fun `findLastSearchResults should propagate the UnknownException`() {

        fun go() {
            repo.findLastSearchResults().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.FindLastSearchResultsException.UnknownException
            }
        }

        localRepository.triggerUnknownException = true
        go()
    }
}

private fun ChildToPublish.matches(other: RetrievedChild): Boolean {
    return this.name == other.name &&
            this.user == other.user &&
            this.notes == other.notes &&
            this.location == other.location &&
            this.appearance == other.appearance
}
