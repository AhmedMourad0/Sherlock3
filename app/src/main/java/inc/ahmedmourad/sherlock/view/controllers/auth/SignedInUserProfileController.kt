package inc.ahmedmourad.sherlock.view.controllers.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import dagger.Lazy
import de.hdodenhof.circleimageview.CircleImageView
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.CompleteSignUpControllerFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignInControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileViewModelQualifier
import inc.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.SignedInUserProfileViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class SignedInUserProfileController(args: Bundle) : LifecycleController(args) {

    @BindView(R.id.signed_in_user_profile_picture)
    internal lateinit var pictureImageView: CircleImageView

    @BindView(R.id.signed_in_user_profile_name)
    internal lateinit var nameTextView: MaterialTextView

    @BindView(R.id.signed_in_user_profile_email)
    internal lateinit var emailTextView: MaterialTextView

    @BindView(R.id.signed_in_user_profile_phone_number)
    internal lateinit var phoneNumberTextView: MaterialTextView

    @BindView(R.id.signed_in_user_profile_registration_date)
    internal lateinit var registrationDateTextView: MaterialTextView

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    @field:SignedInUserProfileViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    internal lateinit var completeSignUpControllerFactory: CompleteSignUpControllerFactory

    @Inject
    @field:SignInControllerQualifier
    lateinit var signInController: Lazy<TaggedController>

    private lateinit var viewModel: SignedInUserProfileViewModel

    private lateinit var context: Context

    private var findSignedInUserDisposable by disposable()

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.signedInUserProfileComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_signed_in_user_profile, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(viewModelFactory)[SignedInUserProfileViewModel::class.java]

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        findSignedInUserDisposable = viewModel.signedInUserSingle.subscribe({ resultEither ->
            resultEither.fold(ifLeft = {
                Timber.error(it, it::toString)
                if (it is NoSignedInUserException) {
                    router.setRoot(
                            RouterTransaction.with(signInController.get().controller)
                                    .tag(signInController.get().tag)
                    )
                } else {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }, ifRight = { userEither ->
                userEither.fold(ifLeft = {
                    val taggedController = completeSignUpControllerFactory(it)
                    router.setRoot(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
                }, ifRight = this@SignedInUserProfileController::populateUi)
            })
        }, {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        })
    }

    override fun onDetach(view: View) {
        findSignedInUserDisposable?.dispose()
        super.onDetach(view)
    }

    private fun populateUi(user: SignedInUser) {

        Glide.with(appCtx)
                .load(user.pictureUrl?.value)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(pictureImageView)

        nameTextView.text = user.displayName.value

        emailTextView.text = user.email.value

        phoneNumberTextView.text = context.getString(
                R.string.phone_number_with_country_code,
                user.phoneNumber.countryCode,
                user.phoneNumber.number
        )

        registrationDateTextView.text = dateManager.get().getRelativeDateTimeString(user.registrationDate)
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.signedInUserProfileComponent.release()
        findSignedInUserDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.SignedInUserProfileController"

        fun newInstance(): TaggedController {
            return TaggedController(SignedInUserProfileController(Bundle(0)), CONTROLLER_TAG)
        }
    }
}
