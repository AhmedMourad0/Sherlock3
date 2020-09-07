package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignInBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.interpreters.interactors.localizedMessage
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignInViewModel
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import timber.log.Timber
import timber.log.error
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

    private fun signIn() {
        signInDisposable = viewModel.onSignIn()?.subscribe({ either ->
            if (either is Either.Left) {
                val e = either.a
                when (e) {

                    SignInInteractor.Exception.AccountDoesNotExistOrHasBeenDisabledException,
                    SignInInteractor.Exception.WrongPasswordException,
                    SignInInteractor.Exception.NoInternetConnectionException -> Unit

                    is SignInInteractor.Exception.InternalException -> {
                        Timber.error(e.origin, e::toString)
                    }

                    is SignInInteractor.Exception.UnknownException -> {
                        Timber.error(e.origin, e::toString)
                    }
                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }
        }, {
            Timber.error(it, it::toString)
        })
    }

    private fun signInWithGoogle() {
        signInDisposable = viewModel.onSignInWithGoogle().subscribe({ either ->
            if (either is Either.Left) {
                val e = either.a
                when (e) {

                    SignInWithGoogleInteractor.Exception.AccountHasBeenDisabledException,
                    SignInWithGoogleInteractor.Exception.MalformedOrExpiredCredentialException,
                    SignInWithGoogleInteractor.Exception.EmailAlreadyInUseException,
                    SignInWithGoogleInteractor.Exception.NoResponseException,
                    SignInWithGoogleInteractor.Exception.NoInternetConnectionException -> Unit

                    is SignInWithGoogleInteractor.Exception.InternalException -> {
                        Timber.error(e.origin, e::toString)
                    }

                    is SignInWithGoogleInteractor.Exception.UnknownException -> {
                        Timber.error(e.origin, e::toString)
                    }
                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }
        }, {
            Timber.error(it, it::toString)
        })
    }

    private fun signInWithFacebook() {
        signInDisposable = viewModel.onSignInWithFacebook().subscribe({ either ->
            if (either is Either.Left) {
                val e = either.a
                when (e) {

                    SignInWithFacebookInteractor.Exception.AccountHasBeenDisabledException,
                    SignInWithFacebookInteractor.Exception.MalformedOrExpiredCredentialException,
                    SignInWithFacebookInteractor.Exception.EmailAlreadyInUseException,
                    SignInWithFacebookInteractor.Exception.NoResponseException,
                    SignInWithFacebookInteractor.Exception.NoInternetConnectionException -> Unit

                    is SignInWithFacebookInteractor.Exception.InternalException -> {
                        Timber.error(e.origin, e::toString)
                    }

                    is SignInWithFacebookInteractor.Exception.UnknownException -> {
                        Timber.error(e.origin, e::toString)
                    }
                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }
        }, {
            Timber.error(it, it::toString)
        })
    }

    private fun signInWithTwitter() {
        signInDisposable = viewModel.onSignInWithTwitter().subscribe({ either ->
            if (either is Either.Left) {
                val e = either.a
                when (e) {

                    SignInWithTwitterInteractor.Exception.AccountHasBeenDisabledException,
                    SignInWithTwitterInteractor.Exception.MalformedOrExpiredCredentialException,
                    SignInWithTwitterInteractor.Exception.EmailAlreadyInUseException,
                    SignInWithTwitterInteractor.Exception.NoResponseException,
                    SignInWithTwitterInteractor.Exception.NoInternetConnectionException -> Unit

                    is SignInWithTwitterInteractor.Exception.InternalException -> {
                        Timber.error(e.origin, e::toString)
                    }

                    is SignInWithTwitterInteractor.Exception.UnknownException -> {
                        Timber.error(e.origin, e::toString)
                    }
                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }
        }, {
            Timber.error(it, it::toString)
        })
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
