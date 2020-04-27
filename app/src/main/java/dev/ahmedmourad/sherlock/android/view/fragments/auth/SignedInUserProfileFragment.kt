package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import arrow.core.identity
import com.bumptech.glide.Glide
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignedInUserProfileBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignedInUserProfileViewModel
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignedInUserProfileFragment : Fragment(R.layout.fragment_signed_in_user_profile) {

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    internal lateinit var viewModelFactory: AssistedViewModelFactory<SignedInUserProfileViewModel>

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
        observe(viewModel.signedInUser) { resultEither ->
            resultEither.fold(ifLeft = {
                Timber.error(it, it::toString)
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }, ifRight = { userEither ->
                userEither.fold(ifLeft = ::identity, ifRight = this::populateUi)
            })
        }
    }

    private fun populateUi(user: SignedInUser) {
        binding?.let { b ->

            Glide.with(appCtx)
                    .load(user.pictureUrl?.value)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(b.profilePicture)

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
