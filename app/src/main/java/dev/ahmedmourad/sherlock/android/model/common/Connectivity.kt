package dev.ahmedmourad.sherlock.android.model.common

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import dev.ahmedmourad.sherlock.android.R

internal enum class Connectivity(@StringRes val message: Int, @ColorRes val color: Int, val isIndefinite: Boolean) {
    CONNECTING(R.string.connecting, R.color.colorConnectivitySnackBarConnecting, true),
    CONNECTED(R.string.back_online, R.color.colorConnectivitySnackBarConnected, false),
    DISCONNECTED(R.string.no_connection, R.color.colorConnectivitySnackBarDisconnected, true)
}
