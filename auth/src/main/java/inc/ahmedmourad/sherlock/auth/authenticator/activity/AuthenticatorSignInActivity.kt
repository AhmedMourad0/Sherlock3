package inc.ahmedmourad.sherlock.auth.authenticator.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arrow.core.left
import arrow.core.right
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import inc.ahmedmourad.sherlock.auth.R
import inc.ahmedmourad.sherlock.auth.authenticator.AuthActivityFactory
import inc.ahmedmourad.sherlock.auth.authenticator.bus.AuthenticatorBus
import inc.ahmedmourad.sherlock.domain.exceptions.SignInCancelledException
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error

private const val REQUEST_CODE_GOOGLE_SIGN_IN = 1364

internal class AuthSignInActivity : AppCompatActivity() {

    private lateinit var signInStrategy: SignInStrategy
    private var cancellationDisposable by disposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth_sign_in)

        cancellationDisposable = AuthenticatorBus.signInCancellation.subscribe({
            finishAffinity()
        }, {
            Timber.error(it, it::toString)
        })

        signInStrategy = when (val action = intent.action) {
            ACTION_SIGN_IN_WITH_GOOGLE -> GoogleSignInStrategy()
            ACTION_SIGN_IN_WITH_FACEBOOK -> FacebookSignInStrategy()
            ACTION_SIGN_IN_WITH_TWITTER -> TwitterSignInStrategy()
            else -> throw IllegalArgumentException("Action not supported: $action")
        }
    }

    override fun onStart() {
        super.onStart()
        signInStrategy.initiate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        signInStrategy.handleResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        cancellationDisposable?.dispose()
        super.onDestroy()
    }

    private inner class GoogleSignInStrategy : SignInStrategy {

        override fun initiate() {

            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.google_sign_in_id_token))
                    .requestEmail()
                    .build()

            val signInIntent = GoogleSignIn.getClient(appCtx, signInOptions).signInIntent

            startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
        }

        override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {

            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    AuthenticatorBus.signInCompletion.accept(GoogleAuthProvider.getCredential(
                            task.getResult(ApiException::class.java)?.idToken, null
                    ).right())
                } catch (e: ApiException) {
                    AuthenticatorBus.signInCompletion.accept(e.left())
                } finally {
                    finishAffinity()
                }
            }
        }
    }

    private inner class FacebookSignInStrategy : SignInStrategy {

        private val loginManager = LoginManager.getInstance()
        private val callbackManager = CallbackManager.Factory.create()

        override fun initiate() {

            loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

                override fun onSuccess(loginResult: LoginResult) {
                    AuthenticatorBus.signInCompletion.accept(FacebookAuthProvider.getCredential(
                            loginResult.accessToken.token
                    ).right())
                    finishAffinity()
                }

                override fun onCancel() {
                    AuthenticatorBus.signInCompletion.accept(SignInCancelledException().left())
                    finishAffinity()
                }

                override fun onError(exception: FacebookException) {
                    AuthenticatorBus.signInCompletion.accept(exception.left())
                    finishAffinity()
                }
            })

            loginManager.logInWithReadPermissions(this@AuthSignInActivity, listOf("email", "public_profile"))
        }

        override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private inner class TwitterSignInStrategy : SignInStrategy {

        private val authClient = TwitterAuthClient()

        override fun initiate() {

            val authConfig = TwitterAuthConfig(
                    getString(R.string.twitter_api_key),
                    getString(R.string.twitter_api_key_secret)
            )

            val twitterConfig = TwitterConfig.Builder(appCtx)
                    .twitterAuthConfig(authConfig)
                    .build()

            Twitter.initialize(twitterConfig)

            authClient.authorize(this@AuthSignInActivity, object : Callback<TwitterSession>() {

                override fun success(result: Result<TwitterSession>) {
                    AuthenticatorBus.signInCompletion.accept(TwitterAuthProvider.getCredential(
                            result.data.authToken.token,
                            result.data.authToken.secret
                    ).right())
                    finishAffinity()
                }

                override fun failure(exception: TwitterException) {
                    AuthenticatorBus.signInCompletion.accept(exception.left())
                    finishAffinity()
                }
            })
        }

        override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
            authClient.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object : AuthActivityFactory {

        private const val ACTION_SIGN_IN_WITH_GOOGLE =
                "inc.ahmedmourad.sherlock.auth.authenticator.activity.action.SIGN_IN_WITH_GOOGLE"

        private const val ACTION_SIGN_IN_WITH_FACEBOOK =
                "inc.ahmedmourad.sherlock.auth.authenticator.activity.action.SIGN_IN_WITH_FACEBOOK"

        private const val ACTION_SIGN_IN_WITH_TWITTER =
                "inc.ahmedmourad.sherlock.auth.authenticator.activity.action.SIGN_IN_WITH_TWITTER"

        override fun signInWithGoogle() = createIntent(ACTION_SIGN_IN_WITH_GOOGLE)

        override fun signInWithFacebook() = createIntent(ACTION_SIGN_IN_WITH_FACEBOOK)

        override fun signInWithTwitter() = createIntent(ACTION_SIGN_IN_WITH_TWITTER)

        private fun createIntent(action: String): Intent {
            return Intent(appCtx, AuthSignInActivity::class.java).apply {
                this.action = action
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}

interface SignInStrategy {
    fun initiate()
    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?)
}
