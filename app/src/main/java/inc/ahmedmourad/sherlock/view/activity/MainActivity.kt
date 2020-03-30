package inc.ahmedmourad.sherlock.view.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.toT
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.florent37.shapeofview.shapes.CutCornerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.HomeControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.MainActivityViewModelQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignInControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SignedInUserProfileControllerQualifier
import inc.ahmedmourad.sherlock.domain.exceptions.NoInternetConnectionException
import inc.ahmedmourad.sherlock.domain.exceptions.NoSignedInUserException
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.Connectivity
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.hideSoftKeyboard
import inc.ahmedmourad.sherlock.viewmodel.activity.MainActivityViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

//TODO: use DataBinding instead of ButterKnife
internal class MainActivity : AppCompatActivity() {

    @BindView(R.id.main_content_root)
    internal lateinit var contentRoot: CutCornerView

    @BindView(R.id.main_content_controllers_container)
    internal lateinit var contentControllersContainer: ChangeHandlerFrameLayout

    @BindView(R.id.main_content_overlay)
    internal lateinit var contentOverlay: View

    @BindView(R.id.main_backdrop_controllers_container)
    internal lateinit var backdropControllersContainer: ChangeHandlerFrameLayout

    @BindView(R.id.main_backdrop_scroll_view)
    internal lateinit var backdropScrollView: NestedScrollView

    @BindView(R.id.main_toolbar)
    internal lateinit var toolbar: MaterialToolbar

    @BindView(R.id.main_appbar)
    internal lateinit var appbar: AppBarLayout

    @BindView(R.id.main_dummy_view)
    internal lateinit var dummyView: View

    @Inject
    @field:MainActivityViewModelQualifier
    lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    @field:HomeControllerQualifier
    lateinit var homeController: Lazy<TaggedController>

    @Inject
    @field:SignInControllerQualifier
    lateinit var signInController: Lazy<TaggedController>

    @Inject
    @field:SignedInUserProfileControllerQualifier
    lateinit var signedInUserProfileController: Lazy<TaggedController>

    private var isContentShown = true

    private val foregroundAnimator by lazy(::createForegroundAnimator)

    private var userAuthenticationState: Tuple2<Boolean, Either<Throwable, Either<IncompleteUser, SignedInUser>>>? = null
        set(value) {
            field = value
            invalidateOptionsMenu()
        }

    private lateinit var viewModel: MainActivityViewModel

    private var internetConnectivityDisposable by disposable()
    private var isUserSignedInDisposable by disposable()
    private var signOutDisposable by disposable()

    private lateinit var foregroundRouter: Router

    private lateinit var backdropRouter: Router

    private lateinit var unbinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SherlockComponent.Activities.mainComponent.get().inject(this)

        unbinder = ButterKnife.bind(this)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]

        foregroundRouter = Conductor.attachRouter(this, contentControllersContainer, savedInstanceState)

        if (!foregroundRouter.hasRootController()) {
            foregroundRouter.setRoot(RouterTransaction.with(homeController.get().controller).tag(homeController.get().tag))
        }

        backdropRouter = Conductor.attachRouter(this, backdropControllersContainer, savedInstanceState)

        dummyView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.post(this@MainActivity::hideSoftKeyboard)
            }
        }

        dummyView.requestFocusFromTouch()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backdropControllersContainer.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                Toast.makeText(this, scrollY.toString(), Toast.LENGTH_LONG).show()
            }
        }

        backdropScrollView.post {
            backdropScrollView.updatePadding(bottom = contentRoot.height / CONTENT_COLLAPSE_FACTOR.toInt())
        }
    }

    override fun onStart() {
        super.onStart()

        foregroundAnimator.addUpdateListener {

            val animatedValue = it.animatedValue as Float

            contentRoot.translationY =
                    animatedValue * (contentRoot.height - contentRoot.height / CONTENT_COLLAPSE_FACTOR)

            appbar.elevation =
                    animatedValue * resources.getDimensionPixelSize(R.dimen.defaultAppBarElevation).toFloat()

            backdropScrollView.translationY =
                    (1 - animatedValue) * resources.getDimensionPixelSize(R.dimen.backdropTranslationY).toFloat()

            contentOverlay.alpha =
                    animatedValue * 0.4f
        }

        foregroundAnimator.doOnEnd {
            invalidateOptionsMenu()
            contentOverlay.visibility = if (isContentShown) View.GONE else View.VISIBLE
            contentControllersContainer.isEnabled = isContentShown
        }

        dummyView.requestFocusFromTouch()

        internetConnectivityDisposable = viewModel.internetConnectivityFlowable
                .doOnSubscribe { showConnectivitySnackBar(Connectivity.CONNECTING) }
                .subscribe(this::showConnectivitySnackBar) {
                    Timber.error(it, it::toString)
                }

        isUserSignedInDisposable = viewModel.isUserSignedInSingle
                .flatMap { isSignedIn ->
                    viewModel.findSignedInUserSingle.map { isSignedIn toT it }
                }.subscribe({
                    if (userAuthenticationState == null) {
                        setInitialBackdropController(it)
                    }
                    userAuthenticationState = it
                }, {
                    Timber.error(it, it::toString)
                })
    }

    private fun showConnectivitySnackBar(connectivity: Connectivity) {

        val duration = if (connectivity.isIndefinite) {
            Snackbar.LENGTH_INDEFINITE
        } else {
            Snackbar.LENGTH_SHORT
        }

        Snackbar.make(contentControllersContainer, connectivity.message, duration).apply {

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

    private fun setInitialBackdropController(
            userState: Tuple2<Boolean, Either<Throwable, Either<IncompleteUser, SignedInUser>>>
    ) {

        if (backdropRouter.hasRootController()) {
            return
        }

        val (isSignedIn, resultEither) = userState

        val taggedController = if (isSignedIn) {

            resultEither.fold(ifLeft = {

                if (it is NoSignedInUserException) {
                    signInController.get()
                } else {
                    TODO("retry or sign out controller")
                }

            }, ifRight = { userEither ->
                userEither.fold(ifLeft = {
                    //It's safer to force sign out the user, if he was at this stage for more than one session
                    signOut()
                    null
                }, ifRight = {
                    signedInUserProfileController.get()
                })
            })

        } else {
            signInController.get()
        }

        if (taggedController != null) {
            backdropRouter.setRoot(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val item = menu?.findItem(R.id.main_menu_show_or_hide_backdrop)
        val state = userAuthenticationState

        item?.isEnabled = state != null
        if (state == null) {
            item?.icon = ContextCompat.getDrawable(this, R.drawable.ic_username) // loading icon
            return super.onPrepareOptionsMenu(menu)
        }

        val (isSignedIn, resultEither) = state

        if (isContentShown) {

            item?.icon = if (isSignedIn) {

                resultEither.fold(ifLeft = {

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

                }, ifRight = { userEither ->
                    userEither.fold(ifLeft = {
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

        menu?.findItem(R.id.main_menu_sign_out)?.isVisible = isSignedIn

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_menu_show_or_hide_backdrop -> {
                showOrHideBackdrop()
                true
            }
            R.id.main_menu_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showOrHideBackdrop() {
        isContentShown = !isContentShown
        invalidateOptionsMenu()
        contentControllersContainer.isEnabled = false
        contentOverlay.visibility = View.VISIBLE
        dummyView.requestFocusFromTouch()
        when {
            foregroundAnimator.isStarted -> foregroundAnimator.reverse()
            isContentShown -> foregroundAnimator.reverse()
            else -> foregroundAnimator.start()
        }
    }

    private fun signOut() {
        signOutDisposable = viewModel.signOutSingle.subscribe({ resultEither ->
            resultEither.fold(ifLeft = {
                Timber.error(it, it::toString)
            }, ifRight = {
                invalidateOptionsMenu()
                backdropRouter.setRoot(
                        RouterTransaction.with(signInController.get().controller)
                                .tag(signInController.get().tag)
                )
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
        internetConnectivityDisposable?.dispose()
        isUserSignedInDisposable?.dispose()
        super.onStop()
    }

    override fun onBackPressed() {

        if (foregroundAnimator.isRunning)
            return

        if (isContentShown) {
            if (!foregroundRouter.handleBack()) {
                super.onBackPressed()
            }
        } else {
            if (!backdropRouter.handleBack()) {
                showOrHideBackdrop()
            }
        }
    }

    override fun onDestroy() {
        SherlockComponent.Activities.mainComponent.release()
        foregroundAnimator.cancel()
        internetConnectivityDisposable?.dispose()
        isUserSignedInDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    companion object {

        const val EXTRA_DESTINATION_ID = "inc.ahmedmourad.sherlock.view.activities.extra.DESTINATION_ID"
        const val INVALID_DESTINATION = -1
        private const val CONTENT_COLLAPSE_FACTOR = 6f

        fun createIntent(destinationId: Int): Intent {
            return Intent(appCtx, MainActivity::class.java).apply {
                putExtra(EXTRA_DESTINATION_ID, destinationId)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }
    }
}
