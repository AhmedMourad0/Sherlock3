package dev.ahmedmourad.sherlock.android.di.modules.factories

import android.content.Intent
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.model.children.AppPublishedChild
import dev.ahmedmourad.sherlock.android.services.SherlockService
import javax.inject.Inject

internal interface SherlockServiceIntentFactory : (AppPublishedChild) -> Intent

@Reusable
internal class SherlockServiceIntentFactoryImpl @Inject constructor() : SherlockServiceIntentFactory {
    override fun invoke(child: AppPublishedChild): Intent {
        return SherlockService.createIntent(child)
    }
}
