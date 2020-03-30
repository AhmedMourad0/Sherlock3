package inc.ahmedmourad.sherlock.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import inc.ahmedmourad.sherlock.R

private const val CHANNEL_ID_BACKGROUND_CONTEXT = "inc.ahmedmourad.sherlock.utils.notification.channel.id.BACKGROUND_CONTEXT"

internal fun backgroundContextChannelId(context: Context): String {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        checkNotNull(ContextCompat.getSystemService(context, NotificationManager::class.java))
                .createNotificationChannel(NotificationChannel(
                CHANNEL_ID_BACKGROUND_CONTEXT,
                context.getString(R.string.channel_name_background_context),
                NotificationManager.IMPORTANCE_LOW
        ).apply { description = context.getString(R.string.channel_description_background_context) })

    return CHANNEL_ID_BACKGROUND_CONTEXT
}
