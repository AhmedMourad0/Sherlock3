package inc.ahmedmourad.sherlock.viewmodel.activity

import androidx.lifecycle.ViewModel
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import io.reactivex.android.schedulers.AndroidSchedulers

internal class MainActivityViewModel(signOutInteractor: SignOutInteractor) : ViewModel() {
    val signOutSingle = signOutInteractor().observeOn(AndroidSchedulers.mainThread())
}
