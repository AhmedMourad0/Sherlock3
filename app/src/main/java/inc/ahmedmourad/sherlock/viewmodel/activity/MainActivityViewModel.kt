package inc.ahmedmourad.sherlock.viewmodel.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import io.reactivex.android.schedulers.AndroidSchedulers

internal class MainActivityViewModel(signOutInteractor: SignOutInteractor) : ViewModel() {

    val signOutSingle = signOutInteractor().observeOn(AndroidSchedulers.mainThread())

    class Factory(
            private val signOutInteractor: SignOutInteractor
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(
                    signOutInteractor
            ) as T
        }
    }
}
