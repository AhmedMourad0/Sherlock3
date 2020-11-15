package dev.ahmedmourad.sherlock.children

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
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
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class ChildrenRepositoryImplTests {

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
    fun publish_shouldAddTheChildToTheRemoteRepoAndReturnItAsRetrievedChild() {
        val child = childToPublish()
        repo.publish(child).test().await().assertValue {
            val result = it.orNull()!!
            val retrieved = child.toRetrievedChild(result.id, result.timestamp, result.pictureUrl)
            assertTrue(remoteRepository.allChildren().contains(retrieved))
            assertEquals(1, remoteRepository.allChildren().size)
            child.matches(result)
        }
    }

    @Test
    fun publish_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.publish(childToPublish()).test().await().assertValue {
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
    fun publish_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.publish(childToPublish()).test().await().assertValue {
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
    fun publish_shouldPropagateTheInternalException() {

        fun go() {
            repo.publish(childToPublish()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.PublishException.InternalException
            }
        }

        imageRepository.triggerInternalException = true
        go()
    }

    @Test
    fun publish_shouldPropagateTheUnknownException() {

        fun go() {
            repo.publish(childToPublish()).test().await().assertValue {
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
    fun find_shouldReturnTheChildWithoutWeightWhenItExistsInTheRemoteRepoOnly() {

        val childId = ChildId(UUID.randomUUID().toString())
        val child = childToPublish()

        remoteRepository.publish(
                childId,
                child,
                null
        ).test().await()

        repo.find(childId).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            assertNull(result!!.b)
            assertEquals(childId, result.a.id)
            child.matches(result.a)
        }
    }

    @Test
    fun find_shouldReturnTheChildWithWeightWhenItExistsInTheRemoteAndLocalRepos() {

        val childId = ChildId(UUID.randomUUID().toString())
        val child = childToPublish()

        remoteRepository.publish(
                childId,
                child,
                null
        ).test().await()

        localRepository.replaceAll(
                mapOf(child.toRetrievedChild(
                        childId,
                        System.currentTimeMillis(),
                        null
                ).simplify() to Weight.of((0..100).random() / 100.0).orNull()!!)
        ).test().await()

        repo.find(childId).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            assertNotNull(result!!.b)
            assertEquals(childId, result.a.id)
            child.matches(result.a)
        }
    }

    @Test
    fun find_shouldReturnNullWhenTheChildExistsInTheLocalRepoOnly() {

        val childId = ChildId(UUID.randomUUID().toString())

        localRepository.replaceAll(
                mapOf(childToPublish().toRetrievedChild(
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
    fun find_shouldReturnNullWhenTheChildDoesNotExist() {

        val childId = ChildId(UUID.randomUUID().toString())

        repo.find(childId).test().await().assertValue {
            assertTrue(it is Either.Right)
            val result = it.orNull()
            result == null
        }
    }

    @Test
    fun find_shouldPropagateTheNoSignedInUserException() {

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
    fun find_shouldPropagateTheNoInternetConnectionException() {

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
    fun find_shouldPropagateTheInternalException() {

        val childId = ChildId(UUID.randomUUID().toString())

        remoteRepository.publish(
                childId,
                childToPublish(),
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
        go()
    }

    @Test
    fun find_shouldPropagateTheUnknownException() {

        val childId = ChildId(UUID.randomUUID().toString())

        remoteRepository.publish(
                childId,
                childToPublish(),
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
    fun findAll_shouldReturnPageSizeOfChildrenAtMax() {

        Single.defer {
            remoteRepository.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublish(),
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
                    childToPublish(),
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
    fun findAll_shouldAddTheQueryToTheRemoteRepo() {

        assertEquals(0, remoteRepository.allQueries().size)

        val query = queryFactory(0)

        repo.findAll(query).test().await()

        assertEquals(1, remoteRepository.allQueries().size)

        assertEquals(query, remoteRepository.allQueries()[0])
    }

    @Test
    fun findAll_shouldReplaceTheLocalDbContentsWithItsResults() {

        assertEquals(0, localRepository.allResults().size)

        Single.defer {
            remoteRepository.publish(
                    ChildId(UUID.randomUUID().toString()),
                    childToPublish(),
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
    fun findAll_shouldReturnAnEmptyMapWhenThereAreNoResults() {

        repo.findAll(queryFactory(0)).test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.isEmpty()
        }

        assertEquals(0, localRepository.allResults().size)
    }

    @Test
    fun findAll_shouldPropagateTheNoSignedInUserException() {

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
    fun findAll_shouldPropagateTheNoInternetConnectionException() {

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
    fun findAll_shouldPropagateTheUnknownException() {

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
    fun invalidateAllQueries_shouldClearAllQueriesFromTheRemoteRepo() {

        assertEquals(0, remoteRepository.allQueries().size)

        repo.findAll(queryFactory(0)).test().await()

        assertEquals(1, remoteRepository.allQueries().size)

        repo.invalidateAllQueries().test().await()

        assertEquals(0, remoteRepository.allQueries().size)
    }

    @Test
    fun addInvestigation_shouldAddTheInvestigationToTheRemoteRepoAndReturnIt() {
        val investigation = investigationFactory()
        repo.addInvestigation(investigation).test().await().assertValue {
            val result = it.orNull()!!
            assertTrue(remoteRepository.allInvestigations().contains(investigation))
            assertEquals(1, remoteRepository.allInvestigations().size)
            result == investigation
        }
    }

    @Test
    fun addInvestigation_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            repo.addInvestigation(investigationFactory()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.AddInvestigationException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun addInvestigation_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            repo.addInvestigation(investigationFactory()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == ChildrenRepository.AddInvestigationException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun addInvestigation_shouldPropagateTheUnknownException() {

        fun go() {
            repo.addInvestigation(investigationFactory()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is ChildrenRepository.AddInvestigationException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun findAllInvestigations_shouldReturnAllOngoingInvestigations() {

        Single.defer {
            remoteRepository.addInvestigation(investigationFactory())
        }.repeat(10).test().await()

        repo.findAllInvestigations().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.size == 10
        }
    }

    @Test
    fun findAllInvestigations_shouldReturnAnEmptyListWhenThereAreNoOngoingInvestigations() {
        repo.findAllInvestigations().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.isEmpty()
        }
    }

    @Test
    fun findAllInvestigations_shouldPropagateTheNoSignedInUserException() {

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
    fun findAllInvestigations_shouldPropagateTheNoInternetConnectionException() {

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
    fun findAllInvestigations_shouldPropagateTheUnknownException() {

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
    fun findLastSearchResults_shouldReturnAllResultsInTheLocalRepoThatHaveWeight() {

        val items = mutableMapOf<SimpleRetrievedChild, Weight>()
        repeat(10) {
            items[childToPublish().toRetrievedChild(
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
    fun findLastSearchResults_shouldReturnAnEmptyMapWhenThereAreNoResults() {

        repo.findLastSearchResults().test().await().assertValue {
            val result = it.orNull()
            assertNotNull(result)
            result!!.isEmpty()
        }

        assertEquals(0, localRepository.allResults().size)
    }

    @Test
    fun findLastSearchResults_shouldPropagateTheUnknownException() {

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

private fun childToPublish(): ChildToPublish {
    return ChildToPublish.of(
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
}

private fun queryFactory(page: Int): ChildrenQuery {
    return ChildrenQuery.of(
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

private fun investigationFactory(): Investigation {
    return queryFactory(0).toInvestigation()
}

private fun ChildToPublish.matches(other: RetrievedChild): Boolean {
    return this.name == other.name &&
            this.user == other.user &&
            this.notes == other.notes &&
            this.location == other.location &&
            this.appearance == other.appearance
}
