package inc.ahmedmourad.sherlock.viewmodel.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.utils.toLiveData
import io.reactivex.android.schedulers.AndroidSchedulers

internal class GlobalViewModel(
        observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
        observeSignedInUserInteractor: ObserveSignedInUserInteractor
) : ViewModel() {

    val internetConnectivity: LiveData<Either<Throwable, Boolean>> =
            observeInternetConnectivityInteractor()
                    .retry()
                    .map<Either<Throwable, Boolean>> { it.right() }
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    val userAuthState: LiveData<Either<Throwable, Boolean>> = observeUserAuthStateInteractor()
            .map<Either<Throwable, Boolean>> { it.right() }
            .onErrorReturn { it.left() }
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()

    val signedInUser: LiveData<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            observeSignedInUserInteractor()
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()
}
