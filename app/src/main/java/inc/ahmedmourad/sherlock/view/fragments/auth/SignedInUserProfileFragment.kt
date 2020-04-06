package inc.ahmedmourad.sherlock.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileViewModelQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentSignedInUserProfileBinding
import inc.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.SignedInUserProfileViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignedInUserProfileFragment : Fragment(R.layout.fragment_signed_in_user_profile) {

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    @field:SignedInUserProfileViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    private lateinit var viewModel: SignedInUserProfileViewModel

    private var findSignedInUserDisposable by disposable()

    private var binding: FragmentSignedInUserProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusSignedInUserProfileFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignedInUserProfileBinding.bind(view)
        viewModel = ViewModelProvider(this, viewModelFactory)[SignedInUserProfileViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        findSignedInUserDisposable = viewModel.signedInUserSingle.subscribe({ resultEither ->
            resultEither.fold(ifLeft = {
                Timber.error(it, it::toString)
                if (it is NoSignedInUserException) {
                    findNavController().navigate(
                            SignedInUserProfileFragmentDirections
                                    .actionSignedInUserProfileFragmentToSignInFragment()
                    )
                } else {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }, ifRight = { userEither ->
                userEither.fold(ifLeft = {
                    findNavController().navigate(
                            SignedInUserProfileFragmentDirections.actionSignedInUserProfileFragmentToCompleteSignUpFragment(
                                    it.bundle(IncompleteUser.serializer())
                            )
                    )
                }, ifRight = this@SignedInUserProfileFragment::populateUi)
            })
        }, {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        })
    }

    override fun onStop() {
        findSignedInUserDisposable?.dispose()
        super.onStop()
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
