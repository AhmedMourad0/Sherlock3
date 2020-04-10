package inc.ahmedmourad.sherlock.view.fragments.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import arrow.core.identity
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.bundlizer.unbundle
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.databinding.FragmentCompleteSignUpBinding
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.CompleteSignUpViewModel
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.CompleteSignUpViewModelFactoryFactory
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
        viewModelFactoryFactory(args.incompleteUser.unbundle(IncompleteUser.serializer()))
    }

    private var completeSignUpDisposable by disposable()

    private val args: CompleteSignUpFragmentArgs by navArgs()
    private var binding: FragmentCompleteSignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusCompleteSignUpFragmentComponent().inject(this)
    }

    //TODO: sign out button
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCompleteSignUpBinding.bind(view)

        initializeEditTexts()
        initializePictureImageView()

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

            b.displayNameEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.displayName.value = s.toString()
                }
            })

            b.emailEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.email.value = s.toString()
                }
            })

            b.phoneNumberEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.phoneNumberCountryCode.value = ""
                    viewModel.phoneNumber.value = s.toString()
                }
            })

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

        imagePicker.get().handleActivityResult(requestCode, data, viewModel.picturePath::setValue)

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
