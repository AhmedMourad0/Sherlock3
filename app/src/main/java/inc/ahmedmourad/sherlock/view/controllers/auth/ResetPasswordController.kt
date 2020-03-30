package inc.ahmedmourad.sherlock.view.controllers.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.ResetPasswordViewModelQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignInControllerQualifier
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.ResetPasswordViewModel
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ResetPasswordController(args: Bundle) : LifecycleController(args), View.OnClickListener {

    @BindView(R.id.reset_password_email_edit_Text)
    internal lateinit var emailEditText: TextInputEditText

    @BindView(R.id.reset_password_send_email_button)
    internal lateinit var sendEmailButton: MaterialButton

    @Inject
    @field:ResetPasswordViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    @field:SignInControllerQualifier
    internal lateinit var signInController: Lazy<TaggedController>

    private lateinit var viewModel: ResetPasswordViewModel

    private lateinit var context: Context

    private var sendEmailDisposable by disposable()

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.resetPasswordComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_reset_password, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(viewModelFactory)[ResetPasswordViewModel::class.java]

        initializeEditTexts()

        arrayOf(sendEmailButton).forEach { it.setOnClickListener(this) }

        return view
    }

    private fun initializeEditTexts() {

        emailEditText.setText(viewModel.email.value)

        emailEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.email.value = s.toString()
            }
        })
    }

    private fun sendPasswordResetEmail() {
        sendEmailDisposable = viewModel.onCompleteSignUp()?.subscribe(::onSendEmailSuccess) {
            Timber.error(it, it::toString)
        }
    }

    private fun onSendEmailSuccess(resultEither: Either<Throwable, Unit>) {
        resultEither.fold(ifLeft = {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        }, ifRight = {
            router.setRoot(RouterTransaction.with(signInController.get().controller).tag(signInController.get().tag))
        })
    }

    override fun onDetach(view: View) {
        sendEmailDisposable?.dispose()
        super.onDetach(view)
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.resetPasswordComponent.release()
        sendEmailDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.reset_password_send_email_button -> sendPasswordResetEmail()
        }
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.ResetPasswordController"

        fun newInstance(): TaggedController {
            return TaggedController(ResetPasswordController(Bundle(0)), CONTROLLER_TAG)
        }
    }
}
