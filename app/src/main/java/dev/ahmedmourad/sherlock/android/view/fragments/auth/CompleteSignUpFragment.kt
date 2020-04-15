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
import arrow.core.Either
import arrow.core.identity
import com.bumptech.glide.Glide
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.dagger.findAppComponent
import dev.ahmedmourad.sherlock.android.databinding.FragmentCompleteSignUpBinding
import dev.ahmedmourad.sherlock.android.utils.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.CompleteSignUpViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.CompleteSignUpViewModelFactoryFactory
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.common.disposable
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class CompleteSignUpFragment : Fragment(R.layout.fragment_complete_sign_up), View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactoryFactory: CompleteSignUpViewModelFactoryFactory

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    private val viewModel: CompleteSignUpViewModel by viewModels {
        viewModelFactoryFactory(this, args.incompleteUser.unbundle(IncompleteUser.serializer()))
    }

    private var completeSignUpDisposable by disposable()

    private val args: CompleteSignUpFragmentArgs by navArgs()
    private var binding: FragmentCompleteSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusCompleteSignUpFragmentComponent().inject(this)
        initializePictureImageView()
    }

    //TODO: sign out button
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCompleteSignUpBinding.bind(view)

        initializeEditTexts()

        binding?.let { b ->
            arrayOf(b.pictureImageView,
                    b.pictureTextView,
                    b.completeButton
            ).forEach { it.setOnClickListener(this) }
        }
    }

    private fun initializeEditTexts() {
        binding?.let { b ->

            b.displayNameEditText.setText(viewModel.displayName.value)
            b.emailEditText.setText(viewModel.email.value)
            b.phoneNumberEditText.setText(
                    appCtx.getString(
                            R.string.phone_number_with_country_code,
                            viewModel.phoneNumberCountryCode.value,
                            viewModel.phoneNumber.value
                    )
            )

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

            if (viewModel.picturePath.value != null) {
                Glide.with(appCtx)
                        .load(viewModel.picturePath.value)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(b.pictureImageView)
            }
        }
    }

    private fun initializePictureImageView() {
        viewModel.picturePath.observe(viewLifecycleOwner, Observer {
            binding?.let { b ->
                Glide.with(appCtx)
                        .load(it)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(b.pictureImageView)
            }
        })
    }

    private fun completeSignUp() {
        completeSignUpDisposable = viewModel.onCompleteSignUp()?.subscribe(::onCompleteSignUpSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun onCompleteSignUpSuccess(resultEither: Either<Throwable, SignedInUser>) {
        resultEither.fold(ifLeft = {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        }, ifRight = ::identity)
    }

    private fun startImagePicker() {
        setPictureEnabled(false)
        imagePicker.get().start(requireActivity()) {
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