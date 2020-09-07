package dev.ahmedmourad.sherlock.android.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.utils.investigationResultsChannelId
import dev.ahmedmourad.sherlock.android.view.activity.MainActivity
import dev.ahmedmourad.sherlock.android.view.fragments.children.ChildDetailsFragmentArgs
import dev.ahmedmourad.sherlock.children.remote.fcm.BaseMessagingService
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import kotlin.math.roundToInt

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MessagingServiceImpl : BaseMessagingService() {

    override fun handleMatchingChildFound(id: ChildId, weight: Weight) {

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setGraph(R.navigation.app_nav_graph)
                .setDestination(R.id.childDetailsFragment)
                .setComponentName(MainActivity::class.java)
                .setArguments(ChildDetailsFragmentArgs(id.bundle(ChildId.serializer())).toBundle())
                .createPendingIntent()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(
                applicationContext,
                investigationResultsChannelId(applicationContext)
        ).setContentTitle(getString(R.string.investigation_result))
                .setContentText(getString(R.string.investigation_result_message, (weight.value * 100).roundToInt()))
                .setSmallIcon(R.drawable.ic_sherlock)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.published_successfully))
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(1000L, 1000L))
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .build()

        checkNotNull(ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
        )).notify((0..65535).random(), notification)
    }
}
