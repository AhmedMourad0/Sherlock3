package dev.ahmedmourad.sherlock.children

import arrow.core.Either
import arrow.core.orNull
import arrow.core.right
import arrow.core.toT
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import dev.ahmedmourad.sherlock.children.local.ChildrenDatabase
import dev.ahmedmourad.sherlock.children.local.database.childrenDatabase
import dev.ahmedmourad.sherlock.children.local.repository.LocalRepositoryImpl
import dev.ahmedmourad.sherlock.children.local.utils.map
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import timber.log.LogcatTree
import timber.log.Timber
import java.util.*

class LocalRepositoryImplTests {

    private lateinit var db: ChildrenDatabase
    private lateinit var repo: LocalRepositoryImpl

    @Before
    fun setup() {

        Timber.plant(LogcatTree("Sherlock"))

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ChildrenDatabase.Schema.create(driver)

        db = childrenDatabase(driver)
        db.childrenQueries.deleteAll()
        db.usersQueries.deleteAll()

        repo = LocalRepositoryImpl(
                db.childrenQueries,
                db.usersQueries
        )
    }

    @Test
    fun insertOrReplaceRetainingWeight_shouldInsertTheChildDataIfItDoesNotExist() {

        val child = retrievedChildFactory()

        assertNull(db.usersQueries.findUserById(child.user.id.value).executeAsOneOrNull())
        assertNull(db.childrenQueries.findChildById(child.id.value).executeAsOneOrNull())

        repo.insertOrReplaceRetainingWeight(child)
                .firstOrError()
                .test()
                .await()
                .assertValue {
                    assertTrue(it.isRight())
                    it as Either.Right
                    it.b.a == child && it.b.b == null
                }

        val (retrievedChild, retrievedWeight) = db.childrenQueries
                .findChildById(child.id.value)
                .executeAsOneOrNull()
                ?.map(child.id)
                ?.orNull() ?: null toT null

        assertEquals(
                child,
                retrievedChild
        )

        assertNull(retrievedWeight)
    }

    @Test
    fun insertOrReplaceRetainingWeight_shouldUpdateTheChildDataRetainingItsWeightIfItAlreadyExists() {

        val results = List(2) {
            retrievedChildFactory() to Weight.of((0..100).random() / 100.0).orNull()!!
        }

        results.forEach { (child, weight) ->

            assertNull(db.usersQueries.findUserById(child.user.id.value).executeAsOneOrNull())
            assertNull(db.childrenQueries.findChildById(child.id.value).executeAsOneOrNull())

            db.usersQueries.insert(
                    child.user.id.value,
                    child.user.displayName.value,
                    child.user.pictureUrl?.value,
                    null,
                    null,
                    null,
                    null,
                    null
            )

            db.childrenQueries.insertOrReplaceSimple(
                    child.id.value,
                    child.user.id.value,
                    child.timestamp,
                    child.name?.orNull()?.first?.value ?: child.name?.swap()?.orNull()?.value,
                    child.name?.orNull()?.last?.value,
                    child.location?.name,
                    child.location?.address,
                    child.pictureUrl?.value,
                    weight.value,
                    child.notes
            )
        }

        fun go(updatedResults: List<RetrievedChild>) {
            require(updatedResults.size == results.size)

            updatedResults.forEachIndexed { index, child ->

                repo.insertOrReplaceRetainingWeight(child)
                        .firstOrError()
                        .test()
                        .await()
                        .assertValue {
                            assertTrue(it.isRight())
                            it as Either.Right
                            it.b.a == child && it.b.b == results[index].second
                        }

                val (retrievedChild, retrievedWeight) = db.childrenQueries
                        .findChildById(child.id.value)
                        .executeAsOneOrNull()
                        ?.map(child.id)
                        ?.orNull() ?: null toT null

                assertEquals(child, retrievedChild)
                assertEquals(results[index].second, retrievedWeight)
            }
        }

        go(results.map { (child, _) ->
            retrievedChildFactory(child.id, child.user.id)
        })

        go(results.map { (child, _) ->
            retrievedChildFactory(child.id)
        })
    }

    @Test
    fun findAllSimpleWhereWeightExists_shouldReturnAllTheResultsThatContainWeight() {

        repo.findAllSimpleWhereWeightExists()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue {
                    assertTrue(it.isRight())
                    it as Either.Right
                    it.b.isEmpty()
                }

        val results = List((7..20).random()) {
            retrievedChildFactory() to Weight.of((0..200).random() / 100.0).orNull()
        }

        results.forEach { (child, weight) ->

            db.usersQueries.insert(
                    child.user.id.value,
                    child.user.displayName.value,
                    child.user.pictureUrl?.value,
                    null,
                    null,
                    null,
                    null,
                    null
            )

            db.childrenQueries.insertOrReplaceSimple(
                    child.id.value,
                    child.user.id.value,
                    child.timestamp,
                    child.name?.orNull()?.first?.value ?: child.name?.swap()?.orNull()?.value,
                    child.name?.orNull()?.last?.value,
                    child.location?.name,
                    child.location?.address,
                    child.pictureUrl?.value,
                    weight?.value,
                    child.notes
            )
        }

        val weighedResults = results.mapNotNull { (child, weight) ->
            weight?.let { child.simplify() to it }
        }.distinct()

        repo.findAllSimpleWhereWeightExists()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    val retrieved = either.b.toList()
                    assertEquals(weighedResults.size, retrieved.size)
                    weighedResults.all { res -> retrieved.any { it == res } }
                }
    }

    @Test
    fun replaceAll_shouldRemoveAllTheChildrenAndUsersAndReplaceThemWithTheNewOnes() {

        val existingResults = List((7..20).random()) {
            retrievedChildFactory() to Weight.of((0..200).random() / 100.0).orNull()
        }

        existingResults.forEach { (child, weight) ->

            db.usersQueries.insert(
                    child.user.id.value,
                    child.user.displayName.value,
                    child.user.pictureUrl?.value,
                    null,
                    null,
                    null,
                    null,
                    null
            )

            db.childrenQueries.insertOrReplaceSimple(
                    child.id.value,
                    child.user.id.value,
                    child.timestamp,
                    child.name?.orNull()?.first?.value ?: child.name?.swap()?.orNull()?.value,
                    child.name?.orNull()?.last?.value,
                    child.location?.name,
                    child.location?.address,
                    child.pictureUrl?.value,
                    weight?.value,
                    child.notes
            )
        }

        assertEquals(existingResults.size, db.childrenQueries.findAllChildren().executeAsList().size)

        val newResults = List((7..20).random()) {
            retrievedChildFactory().simplify() to Weight.of((0..100).random() / 100.0).orNull()!!
        }.toMap()

        repo.replaceAll(newResults).test().await().assertValue { either ->
            assertTrue(either.isRight())
            either as Either.Right
            val retrieved = either.b
            assertEquals(newResults.size, retrieved.size)
            newResults.all { res -> retrieved.any { it == res } }
        }

        val retrievedResults = db.childrenQueries.findAllSimpleWhereWeightExists()
                .executeAsList()
                .mapNotNull { it.map().orNull() }
                .toMap()

        assertEquals(newResults.size, retrievedResults.size)
        assertTrue(newResults.all { res -> retrievedResults.any { it == res } })
    }
}

private fun simpleUserFactory(userId: UserId): SimpleRetrievedUser {
    return SimpleRetrievedUser.of(
            userId,
            DisplayName.of("Ahmed Mourad").orNull()!!,
            null
    )
}

private fun retrievedChildFactory(
        childId: ChildId = ChildId(UUID.randomUUID().toString()),
        userId: UserId = UserId(UUID.randomUUID().toString())
): RetrievedChild {
    return RetrievedChild.of(
            childId,
            simpleUserFactory(userId),
            System.currentTimeMillis(),
            FullName.of(Name.of("Jack").orNull()!!, Name.of("McBigFeet").orNull()!!).right(),
            "Hi there, bye there.",
            Location.of(null, null, null, Coordinates.of((-90..90).random().toDouble(), (-180..180).random().toDouble()).orNull()!!),
            ApproximateAppearance.of(
                    Gender.MALE,
                    Skin.WHEAT,
                    Hair.BROWN,
                    AgeRange.of(Age.of((1..14).random()).orNull()!!, Age.of((15..29).random()).orNull()!!).orNull()!!,
                    HeightRange.of(Height.of((50..110).random()).orNull()!!, Height.of((120..190).random()).orNull()!!).orNull()!!
            ).orNull()!!,
            null
    ).orNull()!!
}
