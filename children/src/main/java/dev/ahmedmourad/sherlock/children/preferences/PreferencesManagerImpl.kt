package dev.ahmedmourad.sherlock.children.preferences

import android.content.Context.MODE_PRIVATE
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.repository.dependencies.PreferencesManager
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject

@Reusable
class PreferencesManagerImpl @Inject constructor() : PreferencesManager {

    private val preferences by lazy {
        appCtx.getSharedPreferences("Prefs", MODE_PRIVATE)
    }

    override fun getDeviceId(): String {

        if (!preferences.contains(KEY_DEVICE_ID)) {
            with(preferences.edit()) {
                putString(KEY_DEVICE_ID, UUID.randomUUID().toString())
                commit()
            }
        }

        return preferences.getString(KEY_DEVICE_ID, null)!!
    }

    companion object {
        private const val KEY_DEVICE_ID =
                "dev.ahmedmourad.sherlock.children.preferences.keys.DEVICE_ID"
    }
}
