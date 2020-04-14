package dev.ahmedmourad.sherlock.android.dagger.modules.factories

import android.content.Intent
import dev.ahmedmourad.sherlock.android.model.children.AppPublishedChild
import dev.ahmedmourad.sherlock.android.services.SherlockService

internal typealias SherlockServiceIntentFactory =
        (@JvmSuppressWildcards AppPublishedChild) -> @JvmSuppressWildcards Intent

internal fun sherlockServiceIntentFactory(child: AppPublishedChild): Intent {
    return SherlockService.createIntent(child)
}
