package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.ViewModel
import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.interactors.auth.FindSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SignedInUserProfileViewModel(interactor: FindSignedInUserInteractor) : ViewModel() {
    val signedInUserSingle: Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            interactor().observeOn(AndroidSchedulers.mainThread())
}
