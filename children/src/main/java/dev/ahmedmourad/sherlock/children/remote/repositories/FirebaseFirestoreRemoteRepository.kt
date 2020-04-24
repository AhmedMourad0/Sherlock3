package dev.ahmedmourad.sherlock.children.remote.repositories

import androidx.annotation.VisibleForTesting
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.children.remote.utils.toMap
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.exceptions.NoInternetConnectionException
import dev.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

@Reusable
internal class FirebaseFirestoreRemoteRepository @Inject constructor(
        @InternalApi private val db: Lazy<FirebaseFirestore>,
        private val authManager: Lazy<AuthManager>,
        private val connectivityManager: Lazy<ConnectivityManager>
) : RemoteRepository {

    init {
        if (FirebaseApp.getApps(appCtx).isEmpty()) {
            FirebaseApp.initializeApp(appCtx)
            db.get().firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
        }
    }

    override fun publish(
            childId: ChildId,
            child: PublishedChild,
            pictureUrl: Url?
    ): Single<Either<Throwable, RetrievedChild>> {

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected)
                        authManager.get().observeUserAuthState().map(Boolean::right).firstOrError()
                    else
                        Single.just(NoInternetConnectionException().left())
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            publishChildData(childId, child, pictureUrl)
                        } else {
                            Single.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun publishChildData(
            childId: ChildId,
            child: PublishedChild,
            pictureUrl: Url?
    ): Single<Either<Throwable, RetrievedChild>> {

        return Single.create<Either<Throwable, RetrievedChild>> { emitter ->

            val successListener = { _: Void ->
                emitter.onSuccess(child.toRetrievedChild(
                        childId,
                        System.currentTimeMillis(),
                        pictureUrl
                ).right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
            }

            db.get().collection(Contract.Database.Children.PATH)
                    .document(childId.value)
                    .set(child.toMap(pictureUrl))
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun find(
            childId: ChildId
    ): Flowable<Either<Throwable, RetrievedChild?>> {
        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        authManager.get().observeUserAuthState().map(Boolean::right)
                    } else {
                        Flowable.just(NoInternetConnectionException().left())
                    }
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createFindFlowable(childId)
                        } else {
                            Flowable.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun createFindFlowable(
            childId: ChildId
    ): Flowable<Either<Throwable, RetrievedChild?>> {

        return Flowable.create<Either<Throwable, RetrievedChild?>>({ emitter ->

            val snapshotListener = { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(exception.left())

                } else if (snapshot != null) {

                    if (snapshot.exists()) {
                        emitter.onNext(extractRetrievedChild(snapshot))
                    } else {
                        emitter.onNext(null.right())
                    }
                }
            }

            val registration = db.get().collection(Contract.Database.Children.PATH)
                    .document(childId.value)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, Map<RetrievedChild, Weight>>> {
        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        authManager.get().observeUserAuthState().map(Boolean::right)
                    } else {
                        Flowable.just(NoInternetConnectionException().left())
                    }
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createFindAllFlowable(filter)
                        } else {
                            Flowable.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun createFindAllFlowable(
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, Map<RetrievedChild, Weight>>> {

        return Flowable.create<Either<Throwable, Map<RetrievedChild, Weight>>>({ emitter ->

            val snapshotListener = { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->

                if (exception != null) {

                    emitter.onNext(exception.left())

                } else if (snapshot != null) {

                    emitter.onNext(filter.filter(snapshot.documents
                            .filter(DocumentSnapshot::exists)
                            .mapNotNull { documentSnapshot ->
                                extractRetrievedChild(documentSnapshot).getOrHandle {
                                    Timber.error(it, it::toString)
                                    null
                                }
                            }
                    ).right())
                }
            }

            //This's all going to change
            val registration = db.get().collection(Contract.Database.Children.PATH)
                    .addSnapshotListener(snapshotListener)

            emitter.setCancellable { registration.remove() }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun clear(): Single<Either<Throwable, Unit>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected)
                        authManager.get().observeUserAuthState().map(Boolean::right).firstOrError()
                    else
                        Single.just(NoInternetConnectionException().left())
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        if (it)
                            deleteChildren()
                        else
                            Single.just(NoSignedInUserException().left())
                    })
                }
    }

    private fun deleteChildren(): Single<Either<Throwable, Unit>> {

        return Single.create<Either<Throwable, Unit>> { emitter ->

            val successListener = { _: Void ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
            }

            val querySuccessListener: (QuerySnapshot) -> Unit = { snapshot: QuerySnapshot ->
                Tasks.whenAll(snapshot.documents.map { it.reference.delete() })
                        .addOnSuccessListener(successListener)
                        .addOnFailureListener(failureListener)
            }

            db.get().collection(Contract.Database.Children.PATH)
                    .get()
                    .addOnSuccessListener(querySuccessListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun extractRetrievedChild(snapshot: DocumentSnapshot): Either<Throwable, RetrievedChild> {

    val id = snapshot.id

    val publicationDate = snapshot.getTimestamp(Contract.Database.Children.PUBLICATION_DATE)
            ?.seconds
            ?.let { it * 1000L }
            ?: return ModelCreationException("publicationDate is null for id=\"$id\"").left()

    val pictureUrl = snapshot.getString(Contract.Database.Children.PICTURE_URL)
            ?.let(Url.Companion::of)
            ?.mapLeft { ModelCreationException(it.toString()) }
            ?.getOrHandle {
                Timber.error(it, it::toString)
                null
            }

    val name = extractName(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    val location = extractLocation(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    return Either.fx {

        val (appearance) = extractApproximateAppearance(snapshot)

        RetrievedChild.of(
                ChildId(id),
                publicationDate,
                name,
                snapshot.getString(Contract.Database.Children.NOTES),
                location,
                appearance,
                pictureUrl
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractName(snapshot: DocumentSnapshot): Either<Throwable, Either<Name, FullName>?> {
    return Either.fx {

        val first = snapshot.getString(Contract.Database.Children.FIRST_NAME) ?: return@fx null

        val (firstName) = Name.of(first).mapLeft { ModelCreationException(it.toString()) }

        val last = snapshot.getString(Contract.Database.Children.LAST_NAME)
                ?: return@fx firstName.left()

        val (lastName) = Name.of(last).mapLeft { ModelCreationException(it.toString()) }

        FullName.of(firstName, lastName)
                .bimap(
                        leftOperation = { ModelCreationException(it.toString()) },
                        rightOperation = FullName::right
                ).bind()
    }
}

private fun extractApproximateAppearance(snapshot: DocumentSnapshot): Either<Throwable, ApproximateAppearance> {
    return Either.fx {

        val gender = snapshot.getLong(Contract.Database.Children.GENDER)?.toInt()
                ?.let { findEnum(it, Gender.values()) }

        val skin = snapshot.getLong(Contract.Database.Children.SKIN)?.toInt()
                ?.let { findEnum(it, Skin.values()) }

        val hair = snapshot.getLong(Contract.Database.Children.HAIR)?.toInt()
                ?.let { findEnum(it, Hair.values()) }

        val (ageRange) = extractAgeRange(snapshot).mapLeft { ModelCreationException(it.toString()) }

        val (heightRange) = extractHeightRange(snapshot).mapLeft { ModelCreationException(it.toString()) }

        ApproximateAppearance.of(
                gender,
                skin,
                hair,
                ageRange,
                heightRange
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractAgeRange(snapshot: DocumentSnapshot): Either<Throwable, AgeRange?> {
    return Either.fx {

        val min = snapshot.getLong(Contract.Database.Children.MIN_AGE)?.toInt() ?: return@fx null

        val (minAge) = Age.of(min).mapLeft { ModelCreationException(it.toString()) }

        val max = snapshot.getLong(Contract.Database.Children.MAX_AGE)?.toInt() ?: return@fx null

        val (maxAge) = Age.of(max).mapLeft { ModelCreationException(it.toString()) }

        AgeRange.of(minAge, maxAge).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractHeightRange(snapshot: DocumentSnapshot): Either<Throwable, HeightRange?> {
    return Either.fx {

        val min = snapshot.getLong(Contract.Database.Children.MIN_HEIGHT)?.toInt() ?: return@fx null

        val (minHeight) = Height.of(min).mapLeft { ModelCreationException(it.toString()) }

        val max = snapshot.getLong(Contract.Database.Children.MAX_HEIGHT)?.toInt() ?: return@fx null

        val (maxHeight) = Height.of(max).mapLeft { ModelCreationException(it.toString()) }

        HeightRange.of(minHeight, maxHeight).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractLocation(snapshot: DocumentSnapshot): Either<Throwable, Location?> {
    return Either.fx {

        val locationId = snapshot.getString(Contract.Database.Children.LOCATION_ID)
                ?: return@fx null
        val locationName = snapshot.getString(Contract.Database.Children.LOCATION_NAME)
                ?: return@fx null
        val locationAddress = snapshot.getString(Contract.Database.Children.LOCATION_ADDRESS)
                ?: return@fx null

        val coordinates = extractCoordinates(snapshot).bind() ?: return@fx null

        Location.of(
                locationId,
                locationName,
                locationAddress,
                coordinates
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractCoordinates(snapshot: DocumentSnapshot): Either<Throwable, Coordinates?> {

    val latitude = snapshot.getDouble(Contract.Database.Children.LOCATION_LATITUDE)
            ?: return null.right()
    val longitude = snapshot.getDouble(Contract.Database.Children.LOCATION_LONGITUDE)
            ?: return null.right()

    return Coordinates.of(latitude, longitude).mapLeft { ModelCreationException(it.toString()) }
}
