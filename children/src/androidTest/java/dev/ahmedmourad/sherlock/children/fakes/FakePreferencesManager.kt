package dev.ahmedmourad.sherlock.children.fakes

import dev.ahmedmourad.sherlock.children.repository.dependencies.PreferencesManager
import java.util.*

internal class FakePreferencesManager : PreferencesManager {

    private var deviceId = UUID.randomUUID().toString()

    override fun getDeviceId(): String {
        return deviceId
    }

    fun invalidateDeviceId() {
        deviceId = UUID.randomUUID().toString()
    }
}
