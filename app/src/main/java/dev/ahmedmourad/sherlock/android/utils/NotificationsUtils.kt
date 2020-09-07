package dev.ahmedmourad.sherlock.android.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dev.ahmedmourad.sherlock.android.R

private const val CHANNEL_ID_BACKGROUND_CONTEXT =
        "dev.ahmedmourad.sherlock.android.utils.notification.channel.id.BACKGROUND_CONTEXT"

private const val CHANNEL_ID_INVESTIGATION_RESULTS =
        "dev.ahmedmourad.sherlock.android.utils.notification.channel.id.INVESTIGATION_RESULTS"

internal fun backgroundContextChannelId(context: Context): String {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        checkNotNull(ContextCompat.getSystemService(context, NotificationManager::class.java))
                .createNotificationChannel(NotificationChannel(
                        CHANNEL_ID_BACKGROUND_CONTEXT,
                        context.getString(R.string.background_context),
                        NotificationManager.IMPORTANCE_LOW
                ).apply { description = context.getString(R.string.channel_description_background_context) })

    return CHANNEL_ID_BACKGROUND_CONTEXT
}

internal fun investigationResultsChannelId(context: Context): String {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        checkNotNull(ContextCompat.getSystemService(context, NotificationManager::class.java))
                .createNotificationChannel(NotificationChannel(
                        CHANNEL_ID_INVESTIGATION_RESULTS,
                        context.getString(R.string.investigation_results),
                        NotificationManager.IMPORTANCE_HIGH
                ).apply { description = context.getString(R.string.channel_description_investigation_results) })

    return CHANNEL_ID_INVESTIGATION_RESULTS
}
