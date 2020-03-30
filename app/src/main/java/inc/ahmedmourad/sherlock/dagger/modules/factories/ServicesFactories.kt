package inc.ahmedmourad.sherlock.dagger.modules.factories

import android.content.Intent
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.services.SherlockService

internal typealias SherlockServiceIntentFactory =
        (@JvmSuppressWildcards AppPublishedChild) -> @JvmSuppressWildcards Intent

internal fun sherlockServiceIntentFactory(child: AppPublishedChild): Intent {
    return SherlockService.createIntent(child)
}
