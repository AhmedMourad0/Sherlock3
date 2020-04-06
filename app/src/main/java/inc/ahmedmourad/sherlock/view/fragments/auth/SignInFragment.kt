package inc.ahmedmourad.sherlock.view.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignInViewModelQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentSignInBinding
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.SignInViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignInFragment : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    @Inject
    @field:SignInViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    private lateinit var viewModel: SignInViewModel

    private var signInDisposable by disposable()

    private var binding: FragmentSignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusSignInFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignInBinding.bind(view)
        viewModel = ViewModelProvider(this, viewModelFactory)[SignInViewModel::class.java]
        initializeEditTexts()
        binding?.let { b ->
            arrayOf(b.signInButton,
                    b.forgotPasswordTextView,
                    b.signUpTextView,
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

            b.emailEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.email.value = s.toString()
                }
            })

            b.passwordEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.password.value = s.toString()
                }
            })
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
        }, ifRight = { either ->
            either.fold(ifLeft = {
                findNavController().navigate(
                        SignInFragmentDirections
                                .actionSignInFragmentToCompleteSignUpFragment(it.bundle(IncompleteUser.serializer()))
                )
            }, ifRight = {
                findNavController().navigate(
                        SignInFragmentDirections
                                .actionSignInFragmentToSignedInUserProfileFragment()
                )
            })
        })
    }

    override fun onStop() {
        signInDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.sign_in_button -> signIn()

            R.id.sign_in_with_google_image_view -> signInWithGoogle()

            R.id.sign_in_with_facebook_image_view -> signInWithFacebook()

            R.id.sign_in_with_twitter_image_view -> signInWithTwitter()

            R.id.sign_up_text_view -> {
                findNavController().navigate(
                        SignInFragmentDirections
                                .actionSignInControllerToSignUpFragment()
                )
            }

            R.id.forgot_password_text_view -> {
                findNavController().navigate(
                        SignInFragmentDirections
                                .actionSignInFragmentToResetPasswordFragment()
                )
            }
        }
    }
}
