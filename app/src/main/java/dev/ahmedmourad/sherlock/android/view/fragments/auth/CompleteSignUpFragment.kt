package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.hbb20.CountryCodePicker
import dagger.Lazy
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentCompleteSignUpBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.CompleteSignUpViewModel
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.utils.exhaust
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

    private val args: CompleteSignUpFragmentArgs by navArgs()
    private var binding: FragmentCompleteSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCompleteSignUpBinding.bind(view)
        (requireActivity() as BackdropActivity).setBackdropTitle(getString(R.string.complete_sign_up))

        initializeEditTexts()
        initializePictureImageView()
        addErrorObservers()

        observe(viewModel.completeSignUpState, Observer { state ->
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (state) {

                is CompleteSignUpViewModel.CompleteSignUpState.Success -> Unit

                CompleteSignUpViewModel.CompleteSignUpState.NoSignedInUser -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    Toast.makeText(context, R.string.authentication_needed, Toast.LENGTH_LONG).show()
                }

                CompleteSignUpViewModel.CompleteSignUpState.NoInternet -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                    Toast.makeText(context, R.string.internet_connection_needed, Toast.LENGTH_LONG).show()
                }

                CompleteSignUpViewModel.CompleteSignUpState.Error -> {
                    Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                }

                null -> Unit
            }.exhaust()
            if (state != null) {
                viewModel.onCompleteSignUpStateHandled()
            }
        })

        binding?.let { b ->
            arrayOf(b.childPicture,
                    b.pictureTextView,
                    b.completeButton
            ).forEach { it.setOnClickListener(this) }
            b.root.requestFocus()
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.emailError,
                viewModel.displayNameError,
                viewModel.phoneNumberError,
                viewModel.pictureError,
                viewModel.userError, observer = Observer { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onEmailErrorHandled()
                viewModel.onDisplayNameErrorHandled()
                viewModel.onPhoneNumberErrorHandled()
                viewModel.onPicturePathErrorHandled()
                viewModel.onUserErrorHandled()
            }
        })
    }

    private fun initializeEditTexts() {
        binding?.let { b ->

            fun CountryCodePicker.unsafeFullNumberWithPlus(): String {
                return "+${this.selectedCountryCode}${b.phoneNumberEditText.text}"
            }

            b.countryCodePicker.registerCarrierNumberEditText(b.phoneNumberEditText)

            b.countryCodePicker.setOnCountryChangeListener {
                viewModel.onPhoneNumberChange(b.countryCodePicker.unsafeFullNumberWithPlus())
            }

            if (viewModel.phoneNumber.value?.isBlank() == false) {
                b.countryCodePicker.fullNumber = viewModel.phoneNumber.value
            }

            b.displayNameEditText.setText(viewModel.displayName.value)
            b.emailEditText.setText(viewModel.email.value)

            b.phoneNumberEditText.doOnTextChanged { _, _, _, _ ->
                viewModel.onPhoneNumberChange(b.countryCodePicker.unsafeFullNumberWithPlus())
            }

            b.displayNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onDisplayNameChange(text.toString())
            }

            b.emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onEmailChange(text.toString())
            }

            if (viewModel.picture.value != null) {
                imageLoader.get().load(
                        viewModel.picture.value?.value,
                        b.childPicture,
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
            }
        }
    }

    private fun initializePictureImageView() {
        observe(viewModel.picture, Observer { picturePath ->
            binding?.let { b ->
                imageLoader.get().load(
                        picturePath?.value,
                        b.childPicture,
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
            }
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
            b.childPicture.isEnabled = enabled
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.complete_button -> viewModel.onCompleteSignUp()
            R.id.picture_text_view, R.id.child_picture -> startImagePicker()
        }
    }
}
