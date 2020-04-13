package inc.ahmedmourad.sherlock.view.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import arrow.core.identity
import arrow.core.right
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignUpViewModelFactoryFactoryQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentSignUpBinding
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.viewmodel.factory.SimpleViewModelFactoryFactory
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.SignUpViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignUpFragment : Fragment(R.layout.fragment_sign_up), View.OnClickListener {

    @Inject
    @field:SignUpViewModelFactoryFactoryQualifier
    internal lateinit var viewModelFactory: SimpleViewModelFactoryFactory

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    private val viewModel: SignUpViewModel by viewModels { viewModelFactory(this) }

    private var signUpDisposable by disposable()

    private var binding: FragmentSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusSignUpFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)
        initializeEditTexts()
        initializePictureImageView()
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

    private fun initializeEditTexts() {
        binding?.let { b ->

            b.displayNameEditText.setText(viewModel.displayName.value)
            b.emailEditText.setText(viewModel.email.value)
            b.passwordEditText.setText(viewModel.password.value)
            b.confirmPasswordEditText.setText(viewModel.passwordConfirmation.value)
            b.phoneNumberEditText.setText(viewModel.phoneNumber.value)

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

    private fun signUp() {
        signUpDisposable = viewModel.onSignUp()?.map {
            it.map(SignedInUser::right)
        }?.subscribe(::onSignUpSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun signUpWithGoogle() {
        signUpDisposable = viewModel.onSignUpWithGoogle().subscribe(::onSignUpSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun signUpWithFacebook() {
        signUpDisposable = viewModel.onSignUpWithFacebook().subscribe(::onSignUpSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun signUpWithTwitter() {
        signUpDisposable = viewModel.onSignUpWithTwitter().subscribe(::onSignUpSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun onSignUpSuccess(resultEither: Either<Throwable, Either<IncompleteUser, SignedInUser>>) {
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
