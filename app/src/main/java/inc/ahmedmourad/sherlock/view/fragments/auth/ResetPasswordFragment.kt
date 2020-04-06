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
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.ResetPasswordViewModelQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentResetPasswordBinding
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.ResetPasswordViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password), View.OnClickListener {

    @Inject
    @field:ResetPasswordViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    private lateinit var viewModel: ResetPasswordViewModel

    private var sendEmailDisposable by disposable()

    private var binding: FragmentResetPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusResetPasswordFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)
        viewModel = ViewModelProvider(this, viewModelFactory)[ResetPasswordViewModel::class.java]
        initializeEditTexts()
        binding?.let { b ->
            arrayOf(b.sendEmailButton).forEach { it.setOnClickListener(this) }
        }
    }

    private fun initializeEditTexts() {
        binding?.let { b ->
            b.emailEditText.setText(viewModel.email.value)
            b.emailEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.email.value = s.toString()
                }
            })
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
