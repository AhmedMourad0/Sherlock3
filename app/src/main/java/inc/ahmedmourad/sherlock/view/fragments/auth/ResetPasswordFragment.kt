package inc.ahmedmourad.sherlock.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.ResetPasswordViewModelFactoryFactoryQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentResetPasswordBinding
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.viewmodel.factory.SimpleViewModelFactoryFactory
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.ResetPasswordViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password), View.OnClickListener {

    @Inject
    @field:ResetPasswordViewModelFactoryFactoryQualifier
    internal lateinit var viewModelFactory: SimpleViewModelFactoryFactory

    private val viewModel: ResetPasswordViewModel by viewModels { viewModelFactory(this) }

    private var sendEmailDisposable by disposable()

    private var binding: FragmentResetPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusResetPasswordFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)
        initializeEditTexts()
        binding?.let { b ->
            arrayOf(b.sendEmailButton).forEach { it.setOnClickListener(this) }
        }
    }

    private fun initializeEditTexts() {
        binding?.let { b ->
            b.emailEditText.setText(viewModel.email.value)
            b.emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onEmailChange(text.toString())
            }
        }
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
            findNavController().popBackStack()
        })
    }

    override fun onStop() {
        sendEmailDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.send_email_button -> sendPasswordResetEmail()
        }
    }
}
