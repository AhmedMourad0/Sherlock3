package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentSignUpBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.interpreters.interactors.localizedMessage
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.SignUpViewModel
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignUpInteractor
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class SignUpFragment : Fragment(R.layout.fragment_sign_up), View.OnClickListener {

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    @Inject
    internal lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<SignUpViewModel>>

    private val viewModel: SignUpViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                SignUpViewModel.defaultArgs()
        )
    }

    private var signUpDisposable by disposable()

    private var binding: FragmentSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)
        initializeEditTexts()
        initializePictureImageView()
        addErrorObservers()
        binding?.let { b ->
            arrayOf(b.pictureImageView,
                    b.pictureTextView,
                    b.signUpButton,
                    b.orSignInTextView,
                    b.signUpWithGoogleImageView,
                    b.signUpWithFacebookImageView,
                    b.signUpWithTwitterImageView
            ).forEach { it.setOnClickListener(this) }
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.passwordError,
                viewModel.passwordConfirmationError,
                viewModel.emailError,
                viewModel.credentialsError,
                viewModel.displayNameError,
                viewModel.phoneNumberError,
                viewModel.picturePathError,
                viewModel.userError
        ) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onPasswordErrorDismissed()
                viewModel.onPasswordConfirmationErrorDismissed()
                viewModel.onEmailErrorDismissed()
                viewModel.onCredentialsErrorDismissed()
                viewModel.onDisplayNameErrorDismissed()
                viewModel.onPhoneNumberErrorDismissed()
                viewModel.onPicturePathErrorDismissed()
                viewModel.onUserErrorDismissed()
            }
        }
    }

    private fun initializeEditTexts() {
        binding?.let { b ->

            b.displayNameEditText.setText(viewModel.displayName.value)
            b.emailEditText.setText(viewModel.email.value)
            b.passwordEditText.setText(viewModel.password.value)
            b.confirmPasswordEditText.setText(viewModel.passwordConfirmation.value)

            val phoneNumber = viewModel.phoneNumber.value
            val countryCode = viewModel.phoneNumberCountryCode.value
            if (phoneNumber != null && countryCode != null) {
                b.phoneNumberEditText.setText(
                        getString(
                                R.string.phone_number_with_country_code,
                                countryCode,
                                phoneNumber
                        )
                )
            } else {
                b.phoneNumberEditText.text = null
            }

            b.displayNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onDisplayNameChange(text.toString())
            }

            b.emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onEmailChange(text.toString())
            }

            b.passwordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onPasswordChange(text.toString())
            }

            b.confirmPasswordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onPasswordConfirmationChange(text.toString())
            }

            b.phoneNumberEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onPhoneNumberCountryCodeChange("")
                viewModel.onPhoneNumberChange(text.toString())
            }
        }
    }

    private fun initializePictureImageView() {
        observe(viewModel.picturePath) { picturePath ->
            binding?.let { b ->
                imageLoader.get().load(
                        picturePath?.value,
                        b.pictureImageView,
                        R.drawable.placeholder,
                        R.drawable.placeholder

                )
            }
        }
    }

    private fun signUp() {
        signUpDisposable = viewModel.onSignUp()?.subscribe({ either ->
            if (either is Either.Left) {
                val e = either.a
                when (e) {

                    SignUpInteractor.Exception.WeakPasswordException,
                    SignUpInteractor.Exception.MalformedEmailException,
                    is SignUpInteractor.Exception.EmailAlreadyInUseException,
                    SignUpInteractor.Exception.NoInternetConnectionException -> Unit

                    is SignUpInteractor.Exception.InternalException -> {
                        Timber.error(e.origin, e::toString)
                    }

                    is SignUpInteractor.Exception.UnknownException -> {
                        Timber.error(e.origin, e::toString)
                    }
                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }
        }, {
            Timber.error(it, it::toString)
        })
    }

    private fun signUpWithGoogle() {
        signUpDisposable = viewModel.onSignUpWithGoogle().subscribe({ either ->
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

    private fun signUpWithFacebook() {
        signUpDisposable = viewModel.onSignUpWithFacebook().subscribe({ either ->
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

    private fun signUpWithTwitter() {
        signUpDisposable = viewModel.onSignUpWithTwitter().subscribe({ either ->
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

    private fun startImagePicker() {
        setPictureEnabled(false)
        imagePicker.get().start(this) {
            setPictureEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    private fun setPictureEnabled(enabled: Boolean) {
        binding?.let { b ->
            b.pictureImageView.isEnabled = enabled
            b.pictureTextView.isEnabled = enabled
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        setPictureEnabled(true)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        checkNotNull(data) {
            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            "Parameter data is null!"
        }

        imagePicker.get().handleActivityResult(requestCode, data, viewModel::onPicturePathChange)

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        signUpDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.sign_up_button -> signUp()

            R.id.sign_up_with_google_image_view -> signUpWithGoogle()

            R.id.sign_up_with_facebook_image_view -> signUpWithFacebook()

            R.id.sign_up_with_twitter_image_view -> signUpWithTwitter()

            R.id.picture_text_view, R.id.picture_image_view -> startImagePicker()

            R.id.or_sign_in_text_view -> {
                findNavController().popBackStack()
            }
        }
    }
}
