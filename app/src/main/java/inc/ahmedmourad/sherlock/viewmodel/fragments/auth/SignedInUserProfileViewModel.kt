package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.left
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.utils.toLiveData
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SignedInUserProfileViewModel(interactor: ObserveSignedInUserInteractor) : ViewModel() {
    val signedInUser: LiveData<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            interactor().onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()
}
