package dev.ahmedmourad.sherlock.children.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.data.FindSimpleUsers
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable

internal class FakeFindSimpleUsers : FindSimpleUsers {

    val users: MutableList<SimpleRetrievedUser> = mutableListOf()

    var isUserSignedIn = true
    var hasInternet = true
    var triggerUnknownException = false

    override fun invoke(
            ids: Collection<UserId>
    ): Flowable<Either<AuthManager.FindSimpleUsersException, List<SimpleRetrievedUser>>> {
        return Flowable.defer {
            if (triggerUnknownException) {
                Flowable.just(AuthManager.FindSimpleUsersException.UnknownException(RuntimeException()).left())
            } else if (!isUserSignedIn) {
                Flowable.just(AuthManager.FindSimpleUsersException.NoSignedInUserException.left())
            } else if (!hasInternet) {
                Flowable.just(AuthManager.FindSimpleUsersException.NoInternetConnectionException.left())
            } else {
                Flowable.just(users.filter { it.id in ids }.right())
            }
        }
    }
}
