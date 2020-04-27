package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import arrow.core.identity
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignInBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignInViewModel
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.common.disposable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignInFragment : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactory: AssistedViewModelFactory<SignInViewModel>

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

    private fun signIn() {
        signInDisposable = viewModel.onSignIn()?.subscribe(::onSignInSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun signInWithGoogle() {
        signInDisposable = viewModel.onSignInWithGoogle().subscribe(::onSignInSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun signInWithFacebook() {
        signInDisposable = viewModel.onSignInWithFacebook().subscribe(::onSignInSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun signInWithTwitter() {
        signInDisposable = viewModel.onSignInWithTwitter().subscribe(::onSignInSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun onSignInSuccess(resultEither: Either<Throwable, Either<IncompleteUser, SignedInUser>>) {
        resultEither.fold(ifLeft = {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        }, ifRight = ::identity)
    }

    override fun onDestroyView() {
        signInDisposable?.dispose()
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.sign_in_button -> signIn()

            R.id.sign_in_with_google_image_view -> signInWithGoogle()

            R.id.sign_in_with_facebook_image_view -> signInWithFacebook()

            R.id.sign_in_with_twitter_image_view -> signInWithTwitter()

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
