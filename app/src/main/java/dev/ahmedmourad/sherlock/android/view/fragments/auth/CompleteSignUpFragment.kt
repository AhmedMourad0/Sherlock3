package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import arrow.core.identity
import dagger.Lazy
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentCompleteSignUpBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.interpreters.interactors.localizedMessage
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.utils.somethingWentWrong
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.CompleteSignUpViewModel
import dev.ahmedmourad.sherlock.domain.interactors.auth.CompleteSignUpInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class CompleteSignUpFragment : Fragment(R.layout.fragment_complete_sign_up), View.OnClickListener {

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    @Inject
    internal lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<CompleteSignUpViewModel>>

    private val viewModel: CompleteSignUpViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                CompleteSignUpViewModel.defaultArgs(args.incompleteUser.unbundle(IncompleteUser.serializer()))
        )
    }

    private var completeSignUpDisposable by disposable()

    private val args: CompleteSignUpFragmentArgs by navArgs()
    private var binding: FragmentCompleteSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    //TODO: sign out button
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCompleteSignUpBinding.bind(view)

        initializeEditTexts()
        initializePictureImageView()
        addErrorObservers()

        binding?.let { b ->
            arrayOf(b.pictureImageView,
                    b.pictureTextView,
                    b.completeButton
            ).forEach { it.setOnClickListener(this) }
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.emailError,
                viewModel.displayNameError,
                viewModel.phoneNumberError,
                viewModel.pictureError,
                viewModel.userError
        ) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onEmailErrorDismissed()
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

            b.phoneNumberEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onPhoneNumberCountryCodeChange("")
                viewModel.onPhoneNumberChange(text.toString())
            }

            if (viewModel.picture.value != null) {
                imageLoader.get().load(
                        viewModel.picture.value?.value,
                        b.pictureImageView,
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
            }
        }
    }

    private fun initializePictureImageView() {
        observe(viewModel.picture) { picturePath ->
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

    private fun completeSignUp() {
        completeSignUpDisposable = viewModel.onCompleteSignUp()?.subscribe(::onCompleteSignUpSuccess) {
            Timber.error(it, it::toString)
            Toast.makeText(appCtx, somethingWentWrong(it), Toast.LENGTH_LONG).show()
        }
    }

    private fun onCompleteSignUpSuccess(
            resultEither: Either<CompleteSignUpInteractor.Exception, SignedInUser>
    ) {
        resultEither.fold(ifLeft = {
            when (it) {

                CompleteSignUpInteractor.Exception.NoInternetConnectionException -> Unit

                CompleteSignUpInteractor.Exception.NoSignedInUserException -> {
                    Timber.error(message = it::toString)
                }

                is CompleteSignUpInteractor.Exception.InternalException -> {
                    Timber.error(it.origin, it::toString)
                }

                is CompleteSignUpInteractor.Exception.UnknownException -> {
                    Timber.error(it.origin, it::toString)
                }

            }.exhaust()
            Toast.makeText(context, it.localizedMessage(), Toast.LENGTH_LONG).show()
        }, ifRight = ::identity)
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

        if (resultCode != Activity.RESULT_OK)
            return

        checkNotNull(data) {
            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            "Parameter data is null!"
        }

        imagePicker.get().handleActivityResult(requestCode, data, viewModel::onPicturePathChange)

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        completeSignUpDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.complete_button -> completeSignUp()
            R.id.picture_text_view, R.id.picture_image_view -> startImagePicker()
        }
    }
}
