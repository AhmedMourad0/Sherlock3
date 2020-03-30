package inc.ahmedmourad.sherlock.view.controllers.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import arrow.core.Either
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
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileControllerQualifier
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.ParcelableWrapper
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.model.common.parcelize
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.CompleteSignUpViewModel
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.factories.CompleteSignUpViewModelFactoryFactory
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class CompleteSignUpController(args: Bundle) : LifecycleController(args), View.OnClickListener {

    @BindView(R.id.complete_sign_up_username_edit_Text)
    internal lateinit var usernameEditText: TextInputEditText

    @BindView(R.id.complete_sign_up_email_edit_Text)
    internal lateinit var emailEditText: TextInputEditText

    @BindView(R.id.complete_sign_up_phone_number_edit_Text)
    internal lateinit var phoneNumberEditText: TextInputEditText

    @BindView(R.id.complete_sign_up_picture_image_view)
    internal lateinit var pictureImageView: CircleImageView

    @BindView(R.id.complete_sign_up_picture_text_view)
    internal lateinit var pictureTextView: MaterialTextView

    @BindView(R.id.complete_sign_up_complete_button)
    internal lateinit var completeButton: MaterialButton

    @Inject
    internal lateinit var viewModelFactoryFactory: CompleteSignUpViewModelFactoryFactory

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    @Inject
    @field:SignedInUserProfileControllerQualifier
    internal lateinit var signedInUserProfileController: Lazy<TaggedController>

    private lateinit var viewModel: CompleteSignUpViewModel

    private lateinit var context: Context

    private var completeSignUpDisposable by disposable()

    private lateinit var unbinder: Unbinder

    //TODO: sign out button
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.completeSignUpComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_complete_sign_up, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(
                viewModelFactoryFactory(
                        requireNotNull(
                                args.getParcelable<ParcelableWrapper<IncompleteUser>>(ARG_INCOMPLETE_USER)).value
                )
        )[CompleteSignUpViewModel::class.java]

        initializeEditTexts()
        initializePictureImageView()

        arrayOf(pictureImageView,
                pictureTextView,
                completeButton
        ).forEach { it.setOnClickListener(this) }

        return view
    }

    private fun initializeEditTexts() {

        usernameEditText.setText(viewModel.displayName.value)
        emailEditText.setText(viewModel.email.value)
        phoneNumberEditText.setText(
                context.getString(
                        R.string.phone_number_with_country_code,
                        viewModel.phoneNumberCountryCode.value,
                        viewModel.phoneNumber.value
                )
        )

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

        phoneNumberEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.phoneNumberCountryCode.value = ""
                viewModel.phoneNumber.value = s.toString()
            }
        })

        if (viewModel.picturePath.value != null) {
            Glide.with(context)
                    .load(viewModel.picturePath.value)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(pictureImageView)
        }
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

    private fun completeSignUp() {
        completeSignUpDisposable = viewModel.onCompleteSignUp()?.subscribe(::onCompleteSignUpSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun onCompleteSignUpSuccess(resultEither: Either<Throwable, SignedInUser>) {
        resultEither.fold(ifLeft = {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        }, ifRight = {
            router.setRoot(
                    RouterTransaction.with(signedInUserProfileController.get().controller)
                            .tag(signedInUserProfileController.get().tag)
            )
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
        completeSignUpDisposable?.dispose()
        super.onDetach(view)
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.completeSignUpComponent.release()
        completeSignUpDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.complete_sign_up_complete_button -> completeSignUp()

            R.id.complete_sign_up_picture_text_view, R.id.complete_sign_up_picture_image_view -> startImagePicker()
        }
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.CompleteSignUpController"

        private const val ARG_INCOMPLETE_USER = "inc.ahmedmourad.sherlock.view.controllers.arg.INCOMPLETE_USER"

        fun newInstance(incompleteUser: IncompleteUser): TaggedController {
            return TaggedController(CompleteSignUpController(Bundle(1).apply {
                putParcelable(ARG_INCOMPLETE_USER, incompleteUser.parcelize())
            }), CONTROLLER_TAG)
        }
    }
}
