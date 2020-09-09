package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignInBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignInViewModel
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import javax.inject.Inject
import javax.inject.Provider

internal class SignInFragment : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<SignInViewModel>>

    private val viewModel: SignInViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                SignInViewModel.defaultArgs()
        )
    }

    //This's intentionally not disposed in onStop, the reason being that signing in with
    // a provider starts a new activity which causes the current activity to be stopped
    // and thus the observable being disposed before we receive the authentication result
    private var signInDisposable by disposable()

    private var binding: FragmentSignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignInBinding.bind(view)

        initializeEditTexts()
        addErrorObservers()

        observe(viewModel.signInState, Observer { state ->
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (state) {

                SignInViewModel.SignInState.Success -> Unit

                SignInViewModel.SignInState.AccountDisabled -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.account_has_been_disabled, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.MalformedOrExpiredCredential -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.session_has_expired, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.EmailAlreadyInUse -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.email_already_in_use, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.NoResponse -> {
                    Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.NoInternet -> {
                    Toast.makeText(context, R.string.internet_connection_needed, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.AccountDisabledOrDoesNotExist -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.account_disabled_or_does_not_exist, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.WrongPassword -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.wrong_email_or_password, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                SignInViewModel.SignInState.Error -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                    setInteractionsEnabled(true)
                }

                null -> Unit
            }.exhaust()
            if (state != null) {
                viewModel.onSignInStateHandled()
            }
        })

        binding?.let { b ->
            arrayOf(b.signInButton,
                    b.forgotPasswordTextView,
                    b.orSignUpTextView,
                    b.signInWithGoogleImageView,
                    b.signInWithFacebookImageView,
                    b.signInWithTwitterImageView
            ).forEach { it.setOnClickListener(this) }
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.emailError,
                viewModel.passwordError,
                viewModel.credentialsError, observer = Observer { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onEmailErrorHandled()
                viewModel.onPasswordErrorHandled()
                viewModel.onCredentialsErrorHandled()
            }
        })
    }

    private fun initializeEditTexts() {
        binding?.let { b ->

            b.emailEditText.setText(viewModel.email.value)
            b.passwordEditText.setText(viewModel.password.value)

            b.emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onEmailChange(text.toString())
            }

            b.passwordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onPasswordChange(text.toString())
            }
        }
    }

    private fun setInteractionsEnabled(enabled: Boolean) {
        binding?.let { b ->
            b.signInButton.isEnabled = enabled
            b.signInWithGoogleImageView.isEnabled = enabled
            b.signInWithFacebookImageView.isEnabled = enabled
            b.signInWithTwitterImageView.isEnabled = enabled
            b.emailEditText.isEnabled = enabled
            b.passwordEditText.isEnabled = enabled
            b.orSignUpTextView.isEnabled = enabled
            b.forgotPasswordTextView.isEnabled = enabled
        }
    }

    override fun onDestroyView() {
        signInDisposable?.dispose()
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.sign_in_button -> {
                setInteractionsEnabled(false)
                viewModel.onSignIn()
            }

            R.id.sign_in_with_google_image_view -> {
                setInteractionsEnabled(false)
                viewModel.onSignInWithGoogle()
            }

            R.id.sign_in_with_facebook_image_view -> {
                setInteractionsEnabled(false)
                viewModel.onSignInWithFacebook()
            }

            R.id.sign_in_with_twitter_image_view -> {
                setInteractionsEnabled(false)
                viewModel.onSignInWithTwitter()
            }

            R.id.or_sign_up_text_view -> {
                findNavController().navigate(
                        SignInFragmentDirections
                                .actionSignInControllerToSignUpFragment()
                )
            }

            R.id.forgot_password_text_view -> {
                findNavController().navigate(
                        SignInFragmentDirections
                                .actionSignInFragmentToResetPasswordFragment(viewModel.email.value)
                )
            }
        }
    }
}
