package dev.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable
import io.reactivex.Single

internal interface RemoteRepository {

    fun storeSignUpUser(user: RemoteSignUpUser): Single<Either<StoreSignUpUserException, SignedInUser>>

    fun findSignedInUser(id: UserId): Flowable<Either<FindSignedInUserException, SignedInUser?>>

    fun updateUserLastLoginDate(id: UserId): Single<Either<UpdateUserLastLoginDateException, Unit>>

    sealed class StoreSignUpUserException {
        object NoInternetConnectionException : StoreSignUpUserException()
        object NoSignedInUserException : StoreSignUpUserException()
        data class UnknownException(val origin: Throwable) : StoreSignUpUserException()
    }

    sealed class FindSignedInUserException {
        object NoInternetConnectionException : FindSignedInUserException()
        object NoSignedInUserException : FindSignedInUserException()
        data class InternalException(val origin: Throwable) : FindSignedInUserException()
        data class UnknownException(val origin: Throwable) : FindSignedInUserException()
    }

    sealed class UpdateUserLastLoginDateException {
        object NoInternetConnectionException : UpdateUserLastLoginDateException()
        object NoSignedInUserException : UpdateUserLastLoginDateException()
        data class UnknownException(val origin: Throwable) : UpdateUserLastLoginDateException()
    }
}
