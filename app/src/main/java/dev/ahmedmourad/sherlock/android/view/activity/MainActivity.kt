package dev.ahmedmourad.sherlock.android.view.activity

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import arrow.core.getOrElse
import com.google.android.material.snackbar.Snackbar
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.databinding.ActivityMainBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.model.common.Connectivity
import dev.ahmedmourad.sherlock.android.utils.*
import dev.ahmedmourad.sherlock.android.view.BackdropProvider
import dev.ahmedmourad.sherlock.android.view.fragments.auth.CompleteSignUpFragmentArgs
import dev.ahmedmourad.sherlock.android.viewmodel.activity.MainActivityViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.exceptions.NoInternetConnectionException
import dev.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.common.disposable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class MainActivity : AppCompatActivity(), BackdropProvider {

    @Inject
    lateinit var viewModelFactory: AssistedViewModelFactory<MainActivityViewModel>

    @Inject
    lateinit var globalViewModelFactory: AssistedViewModelFactory<GlobalViewModel>

    private val foregroundAnimator by lazy(::createForegroundAnimator)

    private lateinit var appNavHostFragment: Fragment
    private lateinit var authNavHostFragment: Fragment

    private val viewModel: MainActivityViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                MainActivityViewModel.defaultArgs(true)
        )
    }

    private val globalViewModel: GlobalViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                globalViewModelFactory,
                GlobalViewModel.defaultArgs()
        )
    }

    private var signOutDisposable by disposable()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injector.inject(this)

        setSupportActionBar(binding.toolbar)

        setupBackdrop()
        setupNavigation()

        binding.root.post {
            showConnectivitySnackBar(Connectivity.CONNECTING)
        }

        observe(globalViewModel.internetConnectivity) { either ->
            either.fold(ifLeft = {
                showConnectivitySnackBar(Connectivity.CONNECTING)
                Timber.error(it, it::toString)
            }, ifRight = {
                showConnectivitySnackBar(getConnectivity(it))
            })
        }

        observeAll(globalViewModel.userAuthState, globalViewModel.signedInUser) { either ->
            either.fold(ifLeft = {
                invalidateOptionsMenu()
                Timber.error(it, it::toString)
            }, ifRight = {
                invalidateOptionsMenu()
                updateBackdropDestination()
            })
        }

        observe(viewModel.isInPrimaryContentMode) { newValue ->
            invalidateOptionsMenu()
            binding.primaryContentOverlay.visibility = View.VISIBLE
            binding.dummyView.requestFocusFromTouch()
            when {
                newValue || foregroundAnimator.isStarted -> foregroundAnimator.reverse()
                else -> foregroundAnimator.start()
            }
            refreshPrimaryNavigationFragment()
        }
    }

    private fun setupBackdrop() {

        binding.dummyView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.post(this@MainActivity::hideSoftKeyboard)
            }
        }

        binding.dummyView.requestFocusFromTouch()

        binding.backdropScrollView.post {
            binding.backdropScrollView.updatePadding(
                    bottom = binding.primaryContentRoot.height / PRIMARY_CONTENT_COLLAPSE_FACTOR.toInt()
            )
        }
    }

    private fun setupNavigation() {
        appNavHostFragment = supportFragmentManager.findFragmentById(R.id.app_nav_host_fragment)!!
        authNavHostFragment = supportFragmentManager.findFragmentById(R.id.auth_nav_host_fragment)!!
        addOnBackPressedCallback()
    }

    private fun getConnectivity(isConnected: Boolean): Connectivity {
        return if (isConnected) Connectivity.CONNECTED else Connectivity.DISCONNECTED
    }

    private fun addOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (foregroundAnimator.isRunning) {
                    return
                }

                if (viewModel.isInPrimaryContentMode.value!!) {
                    if (!findNavController(R.id.app_nav_host_fragment).popBackStack()) {
                        finish()
                    }
                } else {
                    val navController = findNavController(R.id.auth_nav_host_fragment)
                    if (navController.previousBackStackEntry == null) {
                        setInPrimaryContentMode(true)
                    } else {
                        navController.popBackStack()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()

        foregroundAnimator.addUpdateListener {

            val animatedValue = it.animatedValue as Float

            val primaryContentHeight = binding.primaryContentRoot.height
            val translationYMaxValue =
                    primaryContentHeight - primaryContentHeight / PRIMARY_CONTENT_COLLAPSE_FACTOR
            binding.primaryContentRoot.translationY = animatedValue * translationYMaxValue

            binding.appbar.elevation =
                    animatedValue * resources.getDimensionPixelSize(R.dimen.defaultAppBarElevation).toFloat()

            binding.backdropScrollView.translationY =
                    (1 - animatedValue) * resources.getDimensionPixelSize(R.dimen.backdropTranslationY).toFloat()

            binding.primaryContentOverlay.alpha =
                    animatedValue * 0.4f
        }

        foregroundAnimator.doOnEnd {
            invalidateOptionsMenu()
            binding.primaryContentOverlay.visibility = if (viewModel.isInPrimaryContentMode.value!!) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        binding.dummyView.requestFocusFromTouch()
    }

    private fun showConnectivitySnackBar(connectivity: Connectivity) {

        val duration = if (connectivity.isIndefinite) {
            Snackbar.LENGTH_INDEFINITE
        } else {
            Snackbar.LENGTH_SHORT
        }

        Snackbar.make(binding.root, connectivity.message, duration).apply {

            val snackBarView = view.apply {
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, connectivity.color))
            }

            (snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                else
                    gravity = Gravity.CENTER
            }

        }.show()
    }

    private fun createForegroundAnimator(): ValueAnimator {
        return ValueAnimator.ofFloat(
                0f,
                1f
        ).apply {
            this.duration = 700
            this.interpolator = FastOutSlowInInterpolator()
        }
    }

    private fun updateBackdropDestination() {

        val isUserSignedIn = globalViewModel.userAuthState.value?.getOrElse { false } ?: return
        val userEither = globalViewModel.signedInUser.value ?: return

        val authNavController = findNavController(R.id.auth_nav_host_fragment)
        val authNeutralDestinations = arrayOf(
                R.id.signInFragment,
                R.id.signUpFragment,
                R.id.resetPasswordFragment
        )

        if (isUserSignedIn) {

            userEither.fold(ifLeft = {

                if (authNavController.currentDestination?.id !in authNeutralDestinations) {
                    authNavController.navigate(
                            R.id.signInFragment,
                            null,
                            clearBackStack(authNavController)
                    )
                }

            }, ifRight = { either ->
                either.fold(ifLeft = {
                    if (authNavController.currentDestination?.id != R.id.completeSignUpFragment) {
                        authNavController.navigate(
                                R.id.completeSignUpFragment,
                                CompleteSignUpFragmentArgs(it.bundle(IncompleteUser.serializer())).toBundle(),
                                clearBackStack(authNavController)
                        )
                    }
                }, ifRight = {
                    if (authNavController.currentDestination?.id != R.id.signedInUserProfileFragment) {
                        authNavController.navigate(
                                R.id.signedInUserProfileFragment,
                                null,
                                clearBackStack(authNavController)
                        )
                        setInPrimaryContentMode(true)
                    }
                })
            })

        } else {
            if (authNavController.currentDestination?.id !in authNeutralDestinations) {
                authNavController.navigate(
                        R.id.signInFragment,
                        null,
                        clearBackStack(authNavController)
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val isUserSignedIn = globalViewModel.userAuthState.value?.getOrElse { false }
        val item = menu?.findItem(R.id.main_menu_show_or_hide_backdrop)

        menu?.findItem(R.id.main_menu_sign_out)?.isVisible = isUserSignedIn ?: false

        if (!viewModel.isInPrimaryContentMode.value!!) {
            item?.icon = ContextCompat.getDrawable(this, R.drawable.ic_age) // cancel icon
            return super.onPrepareOptionsMenu(menu)
        }

        val userEither = globalViewModel.signedInUser.value
        val isStateLoaded = isUserSignedIn != null && userEither != null

        item?.isEnabled = isStateLoaded
        if (!isStateLoaded) {
            item?.icon = ContextCompat.getDrawable(this, R.drawable.ic_username) // loading icon
            return super.onPrepareOptionsMenu(menu)
        }

        item?.icon = if (isUserSignedIn == true) {

            userEither!!.fold(ifLeft = {

                when (it) {
                    is NoInternetConnectionException -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_hair) // internet error icon
                    }
                    is NoSignedInUserException -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_hair) // no user error icon
                    }
                    else -> {
                        //This should never happen
                        Timber.error(it, it::toString)
                        ContextCompat.getDrawable(this, R.drawable.ic_hair) // error icon
                    }
                }

            }, ifRight = { either ->
                either.fold(ifLeft = {
                    ContextCompat.getDrawable(this, R.drawable.ic_notes) // profile pic with exclamation mark
                }, ifRight = {
                    ContextCompat.getDrawable(this, R.drawable.ic_gender) // profile pic
                })
            })

        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_location) // sign in icon
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_menu_show_or_hide_backdrop -> {
                setInPrimaryContentMode(!viewModel.isInPrimaryContentMode.value!!)
                true
            }
            R.id.main_menu_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setInPrimaryContentMode(newValue: Boolean) {
        viewModel.onIsInPrimaryModeChange(newValue)
    }

    private fun refreshPrimaryNavigationFragment() {
        if (viewModel.isInPrimaryContentMode.value!!) {
            supportFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(appNavHostFragment)
                    .commitNow()
        } else {
            supportFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(authNavHostFragment)
                    .commitNow()
        }
    }

    private fun signOut() {
        signOutDisposable = viewModel.signOutSingle.subscribe({ resultEither ->
            resultEither.fold(ifLeft = {
                Timber.error(it, it::toString)
            }, ifRight = {

            })
        }, {
            Timber.error(it, it::toString)
        })
    }

    override fun onStop() {
        if (foregroundAnimator.isStarted) {
            foregroundAnimator.end()
        }
        foregroundAnimator.removeAllUpdateListeners()
        foregroundAnimator.removeAllListeners()
        super.onStop()
    }

    companion object {
        private const val PRIMARY_CONTENT_COLLAPSE_FACTOR = 6f
    }
}
