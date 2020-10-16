package dev.ahmedmourad.sherlock.android.view.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.ActivityMainBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.utils.clearBackStack
import dev.ahmedmourad.sherlock.android.utils.findNavController
import dev.ahmedmourad.sherlock.android.utils.hideSoftKeyboard
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.view.fragments.auth.CompleteSignUpFragmentArgs
import dev.ahmedmourad.sherlock.android.viewmodel.activity.MainActivityViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import javax.inject.Inject
import javax.inject.Provider

internal class MainActivity : AppCompatActivity(), BackdropActivity {

    @Inject
    internal lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    lateinit var viewModelFactory: Provider<AssistedViewModelFactory<MainActivityViewModel>>

    @Inject
    lateinit var globalViewModelFactory: Provider<AssistedViewModelFactory<GlobalViewModel>>

    private val foregroundAnimator by lazy(::createForegroundAnimator)

    private lateinit var appNavHostFragment: Fragment
    private lateinit var authNavHostFragment: Fragment

    private lateinit var mainTitle: String
    private lateinit var backdropTitle: String

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

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injector.inject(this)

        mainTitle = getString(R.string.app_name)
        backdropTitle = getString(R.string.app_name)
        setSupportActionBar(binding.toolbar)

        setupBackdrop()
        setupNavigation()

        observe(globalViewModel.userState, Observer { state ->
            when (state) {
                is GlobalViewModel.UserState.Authenticated,
                is GlobalViewModel.UserState.Incomplete,
                GlobalViewModel.UserState.Unauthenticated,
                GlobalViewModel.UserState.Loading -> {
                    invalidateOptionsMenu()
                    updateBackdropDestination()
                }
                GlobalViewModel.UserState.NoInternet,
                GlobalViewModel.UserState.Error -> {
                    invalidateOptionsMenu()
                }
            }.exhaust()
        })

        observe(viewModel.isInPrimaryContentMode, Observer { newValue ->
            refreshTitle()
            invalidateOptionsMenu()
            binding.primaryContentOverlay.visibility = View.VISIBLE
            binding.dummyView.requestFocusFromTouch()
            when {
                newValue || foregroundAnimator.isStarted -> foregroundAnimator.reverse()
                else -> foregroundAnimator.start()
            }
            refreshPrimaryNavigationFragment()
        })
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

            if (viewModel.isInPrimaryContentMode.value == true) {
                supportFragmentManager.beginTransaction()
                        .hide(authNavHostFragment)
                        .commitNow()
            }
        }

        binding.dummyView.requestFocusFromTouch()
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

        val userState = globalViewModel.userState.value ?: return

        val authNavController = findNavController(R.id.auth_nav_host_fragment)
        val authNeutralDestinations = arrayOf(
                R.id.signInFragment,
                R.id.signUpFragment,
                R.id.resetPasswordFragment
        )

        when (userState) {

            is GlobalViewModel.UserState.Authenticated -> {
                if (authNavController.currentDestination?.id != R.id.signedInUserProfileFragment) {
                    authNavController.navigate(
                            R.id.signedInUserProfileFragment,
                            null,
                            clearBackStack(authNavController)
                    )
                    setInPrimaryContentMode(true)
                }
                Unit
            }

            is GlobalViewModel.UserState.Incomplete -> {
                if (authNavController.currentDestination?.id != R.id.completeSignUpFragment) {
                    authNavController.navigate(
                            R.id.completeSignUpFragment,
                            CompleteSignUpFragmentArgs(userState.user.bundle(IncompleteUser.serializer())).toBundle(),
                            clearBackStack(authNavController)
                    )
                }
                Unit
            }

            GlobalViewModel.UserState.Unauthenticated -> {
                if (authNavController.currentDestination?.id !in authNeutralDestinations) {
                    authNavController.navigate(
                            R.id.signInFragment,
                            null,
                            clearBackStack(authNavController)
                    )
                }
                Unit
            }

            GlobalViewModel.UserState.Loading,
            GlobalViewModel.UserState.NoInternet -> Unit

            GlobalViewModel.UserState.Error -> {
                if (authNavController.currentDestination?.id !in authNeutralDestinations) {
                    authNavController.navigate(
                            R.id.signInFragment,
                            null,
                            clearBackStack(authNavController)
                    )
                }
                Unit
            }
        }.exhaust()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val item = menu?.findItem(R.id.main_menu_show_or_hide_backdrop)
                ?.apply { this.isEnabled = true } ?: return super.onPrepareOptionsMenu(menu)

        when (globalViewModel.userState.value) {
            is GlobalViewModel.UserState.Authenticated,
            is GlobalViewModel.UserState.Incomplete -> {
                menu.findItem(R.id.main_menu_sign_out)?.isVisible = true
            }
            else -> {
                menu.findItem(R.id.main_menu_sign_out)?.isVisible = false
            }
        }.exhaust()

        if (!viewModel.isInPrimaryContentMode.value!!) {
            item.icon = ContextCompat.getDrawable(this, R.drawable.ic_cancel)
            return super.onPrepareOptionsMenu(menu)
        }

        when (val userState = globalViewModel.userState.value) {

            is GlobalViewModel.UserState.Authenticated -> {
                item.setActionView(R.layout.item_menu_profile_picture)
                imageLoader.get().load(
                        userState.user.pictureUrl?.value,
                        item.actionView.findViewById(R.id.menu_profile_picture_image),
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
                item.actionView.setOnClickListener {
                    onOptionsItemSelected(item)
                }
            }

            is GlobalViewModel.UserState.Incomplete -> {
                item.setActionView(R.layout.item_menu_profile_picture_with_warning)
                imageLoader.get().load(
                        userState.user.pictureUrl?.value,
                        item.actionView.findViewById(R.id.menu_profile_picture_image),
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
                item.actionView.setOnClickListener {
                    onOptionsItemSelected(item)
                }
            }

            GlobalViewModel.UserState.Unauthenticated -> {
                item.icon = ContextCompat.getDrawable(this, R.drawable.ic_login)
            }

            null, GlobalViewModel.UserState.Loading -> {
                item.isEnabled = false
                item.setActionView(R.layout.content_indeterminate_progress_bar)
                setInPrimaryContentMode(true)
            }

            GlobalViewModel.UserState.NoInternet -> {
                item.isEnabled = false
                item.icon = ContextCompat.getDrawable(this, R.drawable.ic_no_internet)
                setInPrimaryContentMode(true)
            }

            GlobalViewModel.UserState.Error -> {
                item.isEnabled = false
                item.icon = ContextCompat.getDrawable(this, R.drawable.ic_error)
                setInPrimaryContentMode(true)
            }
        }.exhaust()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_menu_show_or_hide_backdrop -> {
                setInPrimaryContentMode(!viewModel.isInPrimaryContentMode.value!!)
                true
            }
            R.id.main_menu_sign_out -> {
                viewModel.onSignOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setInPrimaryContentMode(newValue: Boolean) {
        if (newValue) {
            viewModel.onIsInPrimaryModeChange(newValue)
        } else {
            supportFragmentManager.beginTransaction()
                    .show(authNavHostFragment)
                    .commitNow()
            viewModel.onIsInPrimaryModeChange(newValue)
        }
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

    private fun refreshTitle() {
        binding.toolbar.title = if (viewModel.isInPrimaryContentMode.value != false) {
            mainTitle
        } else {
            backdropTitle
        }
    }

    override fun setTitle(title: String?) {
        mainTitle = title ?: getString(R.string.app_name)
        refreshTitle()
    }

    override fun setBackdropTitle(title: String?) {
        backdropTitle = title ?: getString(R.string.app_name)
        refreshTitle()
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
