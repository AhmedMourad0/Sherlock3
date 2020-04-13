package inc.ahmedmourad.sherlock.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import arrow.core.identity
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileViewModelFactoryFactoryQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentSignedInUserProfileBinding
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.viewmodel.factory.SimpleViewModelFactoryFactory
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.SignedInUserProfileViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignedInUserProfileFragment : Fragment(R.layout.fragment_signed_in_user_profile) {

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    @field:SignedInUserProfileViewModelFactoryFactoryQualifier
    internal lateinit var viewModelFactory: SimpleViewModelFactoryFactory

    private val viewModel: SignedInUserProfileViewModel by viewModels { viewModelFactory(this) }

    private var binding: FragmentSignedInUserProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusSignedInUserProfileFragmentComponent().inject(this)

        viewModel.signedInUser.observe(viewLifecycleOwner, Observer { resultEither ->
            resultEither.fold(ifLeft = {
                Timber.error(it, it::toString)
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }, ifRight = { userEither ->
                userEither.fold(ifLeft = ::identity, ifRight = this::populateUi)
            })
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignedInUserProfileBinding.bind(view)
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
