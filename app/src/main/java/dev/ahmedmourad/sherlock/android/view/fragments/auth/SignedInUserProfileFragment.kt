package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignedInUserProfileBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignedInUserProfileViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import dev.ahmedmourad.sherlock.domain.utils.exhaust
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
    internal lateinit var textFormatter: Lazy<TextFormatter>

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
        (requireActivity() as BackdropActivity).setBackdropTitle(getString(R.string.your_profile))
        observe(globalViewModel.userState, Observer { state ->
            when (state) {

                is GlobalViewModel.UserState.Authenticated -> {
                    populateUi(state.user)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.VISIBLE
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                    }
                }

                is GlobalViewModel.UserState.Incomplete -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.something_went_wrong)
                        b.error.errorIcon.setImageResource(R.drawable.ic_research)
                    }
                    Timber.error { "Incomplete user in the SignedInProfileFragment: ${state.user}" }
                }

                GlobalViewModel.UserState.Unauthenticated -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.sign_in_needed_to_view)
                        b.error.errorIcon.setImageResource(R.drawable.ic_finger_print)
                    }
                }

                GlobalViewModel.UserState.Loading -> {
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.VISIBLE
                    }
                }

                GlobalViewModel.UserState.NoInternet -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.no_internet_connection)
                        b.error.errorIcon.setImageResource(R.drawable.ic_no_internet_colorful)
                    }
                }

                GlobalViewModel.UserState.Error -> {
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.something_went_wrong)
                        b.error.errorIcon.setImageResource(R.drawable.ic_research)
                    }
                }
            }.exhaust()
        })

        binding?.error?.root?.setOnClickListener {
            globalViewModel.onRefresh()
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

            b.phoneNumber.text = textFormatter.get().formatPhoneNumber(user.phoneNumber)

            b.timestamp.text = dateManager.get().getRelativeDateTimeString(user.timestamp)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
