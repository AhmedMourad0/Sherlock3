package inc.ahmedmourad.sherlock.view.activity

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
import androidx.lifecycle.Observer
import arrow.core.getOrElse
import com.google.android.material.snackbar.Snackbar
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.bundlizer.bundle
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.GlobalViewModelQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.MainActivityViewModelQualifier
import inc.ahmedmourad.sherlock.databinding.ActivityMainBinding
import inc.ahmedmourad.sherlock.domain.exceptions.NoInternetConnectionException
import inc.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.Connectivity
import inc.ahmedmourad.sherlock.utils.findNavController
import inc.ahmedmourad.sherlock.utils.hideSoftKeyboard
import inc.ahmedmourad.sherlock.utils.singleTop
import inc.ahmedmourad.sherlock.viewmodel.activity.MainActivityViewModel
import inc.ahmedmourad.sherlock.viewmodel.common.GlobalViewModel
import inc.ahmedmourad.sherlock.viewmodel.factory.SimpleViewModelFactoryFactory
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class MainActivity : AppCompatActivity() {

    @Inject
    @field:MainActivityViewModelQualifier
    lateinit var viewModelFactory: SimpleViewModelFactoryFactory

    @Inject
    @field:GlobalViewModelQualifier
    lateinit var globalViewModelFactory: SimpleViewModelFactoryFactory

    private var isContentShown = true

    private val foregroundAnimator by lazy(::createForegroundAnimator)

    private lateinit var appNavHostFragment: Fragment
    private lateinit var authNavHostFragment: Fragment

    private val viewModel: MainActivityViewModel by viewModels { viewModelFactory(this) }
    private val globalViewModel: GlobalViewModel by viewModels { globalViewModelFactory(this) }

    private var signOutDisposable by disposable()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findAppComponent().plusMainActivityComponent().inject(this)

        setSupportActionBar(binding.toolbar)

        binding.dummyView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.post(this@MainActivity::hideSoftKeyboard)
            }
        }

        binding.dummyView.requestFocusFromTouch()

        binding.backdropScrollView.post {
            binding.backdropScrollView.updatePadding(bottom = binding.contentRoot.height / CONTENT_COLLAPSE_FACTOR.toInt())
        }

        appNavHostFragment = supportFragmentManager.findFragmentById(R.id.app_nav_host_fragment)!!
        authNavHostFragment = supportFragmentManager.findFragmentById(R.id.auth_nav_host_fragment)!!

        addOnBackPressedCallback()

        binding.root.post {
            showConnectivitySnackBar(Connectivity.CONNECTING)
        }
        globalViewModel.internetConnectivity.observe(this, Observer { either ->
            either.fold(ifLeft = {
                Timber.error(it, it::toString)
            }, ifRight = {
                showConnectivitySnackBar(getConnectivity(it))
            })
        })

        globalViewModel.userAuthState.observe(this, Observer { either ->
            either.fold(ifLeft = {
                Timber.error(it, it::toString)
            }, ifRight = {
                invalidateOptionsMenu()
                updateBackdropFragment()
            })
        })

        globalViewModel.signedInUser.observe(this, Observer { either ->
            either.fold(ifLeft = {
                Timber.error(it, it::toString)
            }, ifRight = {
                invalidateOptionsMenu()
                updateBackdropFragment()
            })
        })
    }

    private fun getConnectivity(isConnected: Boolean): Connectivity {
        return if (isConnected) Connectivity.CONNECTED else Connectivity.DISCONNECTED
    }

    private fun addOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (foregroundAnimator.isRunning)
                    return

                if (isContentShown) {
                    if (!findNavController(R.id.app_nav_host_fragment).popBackStack()) {
                        finish()
                    }
                } else {
                    if (!findNavController(R.id.auth_nav_host_fragment).popBackStack()) {
                        toggleBackdrop()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()

        foregroundAnimator.addUpdateListener {

            val animatedValue = it.animatedValue as Float

            binding.contentRoot.translationY =
                    animatedValue * (binding.contentRoot.height - binding.contentRoot.height / CONTENT_COLLAPSE_FACTOR)

            binding.appbar.elevation =
                    animatedValue * resources.getDimensionPixelSize(R.dimen.defaultAppBarElevation).toFloat()

            binding.backdropScrollView.translationY =
                    (1 - animatedValue) * resources.getDimensionPixelSize(R.dimen.backdropTranslationY).toFloat()

            binding.contentOverlay.alpha =
                    animatedValue * 0.4f
        }

        foregroundAnimator.doOnEnd {
            invalidateOptionsMenu()
            binding.contentOverlay.visibility = if (isContentShown) View.GONE else View.VISIBLE
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

    private fun updateBackdropFragment() {

        val isUserSignedIn = globalViewModel.userAuthState.value?.getOrElse { false } ?: return
        val userEither = globalViewModel.signedInUser.value ?: return

        val authNavController = findNavController(R.id.auth_nav_host_fragment)

        if (isUserSignedIn) {

            userEither.fold(ifLeft = {

                if (it is NoSignedInUserException) {
                    authNavController.navigate(R.id.signInFragment, null, singleTop())
                } else {
                    authNavController.navigate(R.id.signInFragment, null, singleTop())
                    TODO("retry or sign out controller")
                }

            }, ifRight = { either ->
                either.fold(ifLeft = {
                    val args = Bundle(1).apply {
                        putBundle("incompleteUser", it.bundle(IncompleteUser.serializer()))
                    }
                    authNavController.navigate(R.id.completeSignUpFragment, args, singleTop())
                    null
                }, ifRight = {
                    authNavController.navigate(R.id.signedInUserProfileFragment, null, singleTop())
                })
            })

        } else {
            authNavController.navigate(R.id.signInFragment, null, singleTop())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val item = menu?.findItem(R.id.main_menu_show_or_hide_backdrop)
        val isUserSignedIn = globalViewModel.userAuthState.value?.getOrElse { false }
        val userEither = globalViewModel.signedInUser.value
        val isStateLoaded = isUserSignedIn != null && userEither != null

        item?.isEnabled = isStateLoaded
        if (!isStateLoaded) {
            item?.icon = ContextCompat.getDrawable(this, R.drawable.ic_username) // loading icon
            return super.onPrepareOptionsMenu(menu)
        }

        if (isContentShown) {

            item?.icon = if (isUserSignedIn!!) {

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

        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_age) // cancel icon
        }

        menu?.findItem(R.id.main_menu_sign_out)?.isVisible = isUserSignedIn!!

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_menu_show_or_hide_backdrop -> {
                toggleBackdrop()
                true
            }
            R.id.main_menu_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleBackdrop() {
        isContentShown = !isContentShown
        invalidateOptionsMenu()
        binding.contentOverlay.visibility = View.VISIBLE
        binding.dummyView.requestFocusFromTouch()
        when {
            foregroundAnimator.isStarted -> foregroundAnimator.reverse()
            isContentShown -> foregroundAnimator.reverse()
            else -> foregroundAnimator.start()
        }
        if (isContentShown) {
            supportFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(appNavHostFragment)
                    .commit()
        } else {
            supportFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(authNavHostFragment)
                    .commit()
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
        private const val CONTENT_COLLAPSE_FACTOR = 6f
    }
}
