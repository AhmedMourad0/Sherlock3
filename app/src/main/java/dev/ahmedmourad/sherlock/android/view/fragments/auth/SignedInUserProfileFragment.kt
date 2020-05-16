package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignedInUserProfileBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.interpreters.interactors.localizedMessage
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignedInUserProfileViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class SignedInUserProfileFragment : Fragment(R.layout.fragment_signed_in_user_profile) {

    @Inject
    internal lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<SignedInUserProfileViewModel>>

    private val globalViewModel: GlobalViewModel by activityViewModels()
    private val viewModel: SignedInUserProfileViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                SignedInUserProfileViewModel.defaultArgs()
        )
    }

    private var binding: FragmentSignedInUserProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignedInUserProfileBinding.bind(view)
        observe(globalViewModel.signedInUser) { resultEither ->
            resultEither.fold(ifLeft = { e ->
                when (e) {

                    ObserveSignedInUserInteractor.Exception.NoInternetConnectionException -> { /* do nothing */
                    }

                    is ObserveSignedInUserInteractor.Exception.InternalException -> {
                        Timber.error(e.origin, e::toString)
                    }

                    is ObserveSignedInUserInteractor.Exception.UnknownException -> {
                        Timber.error(e.origin, e::toString)
                    }

                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }, ifRight = { userEither ->
                if (userEither is Either.Right) {
                    populateUi(userEither.b)
                }
            })
        }
    }

    private fun populateUi(user: SignedInUser) {
        binding?.let { b ->

            imageLoader.get().load(
                    user.pictureUrl?.value,
                    b.profilePicture,
                    R.drawable.placeholder,
                    R.drawable.placeholder
            )

            b.name.text = user.displayName.value

            b.email.text = user.email.value

            b.phoneNumber.text = appCtx.getString(
                    R.string.phone_number_with_country_code,
                    user.phoneNumber.countryCode,
                    user.phoneNumber.number
            )

            b.registrationDate.text = dateManager.get().getRelativeDateTimeString(user.registrationDate)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
