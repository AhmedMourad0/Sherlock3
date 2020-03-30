package inc.ahmedmourad.sherlock.view.controllers.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.CompleteSignUpControllerFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.ResetPasswordControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignInViewModelQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignUpControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileControllerQualifier
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.SignInViewModel
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignInController(args: Bundle) : LifecycleController(args), View.OnClickListener {

    @BindView(R.id.sign_in_email_edit_Text)
    internal lateinit var emailEditText: TextInputEditText

    @BindView(R.id.sign_in_password_edit_Text)
    internal lateinit var passwordEditText: TextInputEditText

    @BindView(R.id.sign_in_sign_in_button)
    internal lateinit var signInButton: MaterialButton

    @BindView(R.id.sign_in_forgot_password_text_view)
    internal lateinit var forgotMyPasswordTextView: MaterialTextView

    @BindView(R.id.sign_in_sign_up_text_view)
    internal lateinit var signUpTextView: MaterialTextView

    @BindView(R.id.sign_in_sign_in_with_google_image_view)
    internal lateinit var signInWithGoogleImageView: ImageView

    @BindView(R.id.sign_in_sign_in_with_facebook_image_view)
    internal lateinit var signInWithFacebookImageView: ImageView

    @BindView(R.id.sign_in_sign_in_with_twitter_image_view)
    internal lateinit var signInWithTwitterImageView: ImageView

    @Inject
    @field:SignInViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    @field:SignUpControllerQualifier
    internal lateinit var signUpController: Lazy<TaggedController>

    @Inject
    @field:ResetPasswordControllerQualifier
    internal lateinit var resetPasswordController: Lazy<TaggedController>

    @Inject
    @field:SignedInUserProfileControllerQualifier
    internal lateinit var signedInUserProfileController: Lazy<TaggedController>

    @Inject
    internal lateinit var completeSignUpControllerFactory: CompleteSignUpControllerFactory

    private lateinit var viewModel: SignInViewModel

    private lateinit var context: Context

    private var signInDisposable by disposable()

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.signInComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_sign_in, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(viewModelFactory)[SignInViewModel::class.java]

        initializeEditTexts()

        arrayOf(signInButton,
                forgotMyPasswordTextView,
                signUpTextView,
                signInWithGoogleImageView,
                signInWithFacebookImageView,
                signInWithTwitterImageView
        ).forEach { it.setOnClickListener(this) }

        return view
    }

    private fun initializeEditTexts() {

        emailEditText.setText(viewModel.email.value)
        passwordEditText.setText(viewModel.password.value)

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
        }, ifRight = { either ->
            either.fold(ifLeft = {
                val taggedController = completeSignUpControllerFactory(it)
                router.setRoot(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
            }, ifRight = {
                router.setRoot(
                        RouterTransaction.with(signedInUserProfileController.get().controller)
                                .tag(signedInUserProfileController.get().tag)
                )
            })
        })
    }

    override fun onDetach(view: View) {
        signInDisposable?.dispose()
        super.onDetach(view)
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.signInComponent.release()
        signInDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.sign_in_sign_in_button -> signIn()

            R.id.sign_in_sign_in_with_google_image_view -> signInWithGoogle()

            R.id.sign_in_sign_in_with_facebook_image_view -> signInWithFacebook()

            R.id.sign_in_sign_in_with_twitter_image_view -> signInWithTwitter()

            R.id.sign_in_sign_up_text_view -> {
                router.pushController(
                        RouterTransaction.with(signUpController.get().controller)
                                .tag(signUpController.get().tag)
                )
            }

            R.id.sign_in_forgot_password_text_view -> {
                router.pushController(
                        RouterTransaction.with(resetPasswordController.get().controller)
                                .tag(resetPasswordController.get().tag)
                )
            }
        }
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.SignInController"

        fun newInstance() = TaggedController(SignInController(Bundle(0)), CONTROLLER_TAG)
    }
}
