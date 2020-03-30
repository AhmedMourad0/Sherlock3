package inc.ahmedmourad.sherlock.dagger.modules.factories

import android.content.Intent
import inc.ahmedmourad.sherlock.view.activity.MainActivity

internal typealias MainActivityIntentFactory =
        (@JvmSuppressWildcards Int) -> @JvmSuppressWildcards Intent

internal fun mainActivityIntentFactory(destinationId: Int): Intent {
    return MainActivity.createIntent(destinationId)
}
