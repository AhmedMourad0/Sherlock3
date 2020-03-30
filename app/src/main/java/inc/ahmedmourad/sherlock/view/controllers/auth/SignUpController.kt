package inc.ahmedmourad.sherlock.view.controllers.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import arrow.core.right
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.Lazy
import de.hdodenhof.circleimageview.CircleImageView
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.CompleteSignUpControllerFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignInControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignUpViewModelQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileControllerQualifier
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.SignUpViewModel
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignUpController(args: Bundle) : LifecycleController(args), View.OnClickListener {

    @BindView(R.id.sign_up_username_edit_Text)
    internal lateinit var usernameEditText: TextInputEditText

    @BindView(R.id.sign_up_email_edit_Text)
    internal lateinit var emailEditText: TextInputEditText

    @BindView(R.id.sign_up_password_edit_Text)
    internal lateinit var passwordEditText: TextInputEditText

    @BindView(R.id.sign_up_confirm_password_edit_Text)
    internal lateinit var confirmPasswordEditText: TextInputEditText

    @BindView(R.id.sign_up_phone_number_edit_Text)
    internal lateinit var phoneNumberEditText: TextInputEditText

    @BindView(R.id.sign_up_picture_image_view)
    internal lateinit var pictureImageView: CircleImageView

    @BindView(R.id.sign_up_picture_text_view)
    internal lateinit var pictureTextView: MaterialTextView

    @BindView(R.id.sign_up_sign_up_button)
    internal lateinit var signUpButton: MaterialButton

    @BindView(R.id.sign_up_sign_in_text_view)
    internal lateinit var signInTextView: MaterialTextView

    @BindView(R.id.sign_up_sign_up_with_google_image_view)
    internal lateinit var signUpWithGoogleImageView: ImageView

    @BindView(R.id.sign_up_sign_up_with_facebook_image_view)
    internal lateinit var signUpWithFacebookImageView: ImageView

    @BindView(R.id.sign_up_sign_up_with_twitter_image_view)
    internal lateinit var signUpWithTwitterImageView: ImageView

    @Inject
    @field:SignUpViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    @field:SignInControllerQualifier
    internal lateinit var signInController: Lazy<TaggedController>

    @Inject
    @field:SignedInUserProfileControllerQualifier
    internal lateinit var signedInUserProfileController: Lazy<TaggedController>

    @Inject
    internal lateinit var completeSignUpControllerFactory: CompleteSignUpControllerFactory

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    private lateinit var viewModel: SignUpViewModel

    private lateinit var context: Context

    private var signUpDisposable by disposable()

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.signUpComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_sign_up, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(viewModelFactory)[SignUpViewModel::class.java]

        initializeEditTexts()
        initializePictureImageView()

        arrayOf(pictureImageView,
                pictureTextView,
                signUpButton,
                signInTextView,
                signUpWithGoogleImageView,
                signUpWithFacebookImageView,
                signUpWithTwitterImageView
        ).forEach { it.setOnClickListener(this) }

        return view
    }

    private fun initializeEditTexts() {

        usernameEditText.setText(viewModel.displayName.value)
        emailEditText.setText(viewModel.email.value)
        passwordEditText.setText(viewModel.password.value)
        confirmPasswordEditText.setText(viewModel.passwordConfirmation.value)
        phoneNumberEditText.setText(viewModel.phoneNumber.value)

        usernameEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.displayName.value = s.toString()
            }
        })

        emailEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.email.value = s.toString()
            }
        })

        passwordEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.password.value = s.toString()
            }
        })

        confirmPasswordEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.passwordConfirmation.value = s.toString()
            }
        })

        phoneNumberEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.phoneNumber.value = s.toString()
            }
        })
    }

    private fun initializePictureImageView() {
        viewModel.picturePath.observe(this, Observer {
            Glide.with(context)
                    .load(it)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(pictureImageView)
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
        }, ifRight = { either ->
            either.fold(ifLeft = {
                val taggedController = completeSignUpControllerFactory(it)
                router.setRoot(
                        RouterTransaction.with(taggedController.controller)
                                .tag(taggedController.tag)
                )
            }, ifRight = {
                router.setRoot(
                        RouterTransaction.with(signedInUserProfileController.get().controller)
                                .tag(signedInUserProfileController.get().tag)
                )
            })
        })
    }

    private fun startImagePicker() {
        setPictureEnabled(false)
        imagePicker.get().start(checkNotNull(activity)) {
            setPictureEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    private fun setPictureEnabled(enabled: Boolean) {
        pictureImageView.isEnabled = enabled
        pictureTextView.isEnabled = enabled
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        setPictureEnabled(true)

        if (resultCode != Activity.RESULT_OK)
            return

        checkNotNull(data) {
            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            "Parameter data is null!"
        }

        imagePicker.get().handleActivityResult(requestCode, data) { pictureEither ->
            pictureEither.fold(ifLeft = {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }, ifRight = viewModel.picturePath::setValue)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDetach(view: View) {
        signUpDisposable?.dispose()
        super.onDetach(view)
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.signUpComponent.release()
        signUpDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.sign_up_sign_up_button -> signUp()

            R.id.sign_up_sign_up_with_google_image_view -> signUpWithGoogle()

            R.id.sign_up_sign_up_with_facebook_image_view -> signUpWithFacebook()

            R.id.sign_up_sign_up_with_twitter_image_view -> signUpWithTwitter()

            R.id.sign_up_picture_text_view, R.id.sign_up_picture_image_view -> startImagePicker()

            R.id.sign_up_sign_in_text_view -> {
                router.setRoot(RouterTransaction.with(signInController.get().controller).tag(signInController.get().tag))
            }
        }
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.SignUpController"

        fun newInstance() = TaggedController(SignUpController(Bundle(0)), CONTROLLER_TAG)
    }
}
