package inc.ahmedmourad.sherlock.viewmodel.activity

import androidx.lifecycle.ViewModel
import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.interactors.auth.FindSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.model.common.Connectivity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers

internal class MainActivityViewModel(
        observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
        findSignedInUserInteractor: FindSignedInUserInteractor,
        signOutInteractor: SignOutInteractor
) : ViewModel() {

    val internetConnectivityFlowable: Flowable<Connectivity> = observeInternetConnectivityInteractor()
            .map(this::getConnectivity)
            .retry()
            .observeOn(AndroidSchedulers.mainThread())

    val isUserSignedInSingle: Flowable<Boolean> = observeUserAuthStateInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    val findSignedInUserSingle: Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            findSignedInUserInteractor().observeOn(AndroidSchedulers.mainThread())

    val signOutSingle = signOutInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    private fun getConnectivity(isConnected: Boolean): Connectivity {
        return if (isConnected) Connectivity.CONNECTED else Connectivity.DISCONNECTED
    }
}
