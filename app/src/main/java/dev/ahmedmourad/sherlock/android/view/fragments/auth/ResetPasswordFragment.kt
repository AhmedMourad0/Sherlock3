package dev.ahmedmourad.sherlock.android.view.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentResetPasswordBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.ResetPasswordViewModel
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import javax.inject.Inject
import javax.inject.Provider

internal class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password), View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<ResetPasswordViewModel>>

    private val viewModel: ResetPasswordViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                ResetPasswordViewModel.defaultArgs(args.email)
        )
    }

    private val args: ResetPasswordFragmentArgs by navArgs()

    private var binding: FragmentResetPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)
        initializeEditTexts()
        addErrorObservers()
        observe(viewModel.passwordResetState, Observer { state ->
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (state) {

                ResetPasswordViewModel.PasswordResetState.Success -> {
                    findNavController().popBackStack()
                }

                ResetPasswordViewModel.PasswordResetState.NonExistentEmail -> {
                    Toast.makeText(context, R.string.email_non_existent, Toast.LENGTH_LONG).show()
                }

                ResetPasswordViewModel.PasswordResetState.NoInternet -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                    Toast.makeText(context, R.string.internet_connection_needed, Toast.LENGTH_LONG).show()
                }

                ResetPasswordViewModel.PasswordResetState.Error -> {
                    Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                }

                null -> Unit
            }.exhaust()
            viewModel.onResetPasswordStateHandled()
        })
        binding?.let { b ->
            arrayOf(b.sendEmailButton).forEach { it.setOnClickListener(this) }
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observe(viewModel.emailError, Observer { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onEmailErrorHandled()
            }
        })
    }

    private fun initializeEditTexts() {
        binding?.let { b ->
            b.emailEditText.setText(viewModel.email.value)
            b.emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onEmailChange(text.toString())
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.send_email_button -> viewModel.onSendResetEmail()
        }
    }
}
