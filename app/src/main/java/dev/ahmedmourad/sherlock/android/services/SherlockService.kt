package dev.ahmedmourad.sherlock.android.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.dagger.findAppComponent
import dev.ahmedmourad.sherlock.android.model.children.AppPublishedChild
import dev.ahmedmourad.sherlock.android.utils.backgroundContextChannelId
import dev.ahmedmourad.sherlock.android.view.activity.MainActivity
import dev.ahmedmourad.sherlock.android.view.fragments.children.AddChildFragmentArgs
import dev.ahmedmourad.sherlock.android.view.fragments.children.ChildDetailsFragmentArgs
import dev.ahmedmourad.sherlock.domain.interactors.children.AddChildInteractor
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.common.disposable
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

//TODO: use WorkManager with Notifications (maybe ListenableWorker with progress?)
// and firebase authentication fallback instead
internal class SherlockService : Service() {

    @Inject
    lateinit var addChildInteractor: AddChildInteractor

    private var addChildDisposable by disposable()

    override fun onCreate() {
        super.onCreate()
        appCtx.findAppComponent().plusSherlockServiceComponent().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        checkNotNull(intent)
        checkNotNull(intent.action)

        when (intent.action) {
            ACTION_PUBLISH_CHILD -> handleActionPublishFound(
                    requireNotNull(
                            intent.getBundleExtra(EXTRA_CHILD)
                    ).unbundle(AppPublishedChild.serializer())
            )
        }

        return START_REDELIVER_INTENT
    }

    private fun handleActionPublishFound(appChild: AppPublishedChild) {

        val child = appChild.toPublishedChild()

        startForeground(NOTIFICATION_ID_PUBLISH_CHILD, createPublishingNotification(appChild))

        addChildDisposable = addChildInteractor(child)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    Toast.makeText(applicationContext, getString(R.string.published_successfully), Toast.LENGTH_LONG).show()
                }.doOnError {
                    Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_LONG).show()
                }.doFinally {
                    stopForeground(true)
                    stopSelf()
                }.subscribe({ childEither ->
                    childEither.fold(ifLeft = {
                        Timber.error(it, it::toString)
                        showPublishingFailedNotification(it, appChild, true)
                    }, ifRight = { child ->
                        this.showPublishedSuccessfullyNotification(child.simplify())
                    })
                }, {
                    Timber.error(it, it::toString)
                    showPublishingFailedNotification(it, appChild, true)
                })
    }

    private fun createPublishingNotification(child: AppPublishedChild): Notification {

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setGraph(R.navigation.app_nav_graph)
                .setDestination(R.id.addChildFragment)
                .setComponentName(MainActivity::class.java)
                .setArguments(AddChildFragmentArgs(child.bundle(AppPublishedChild.serializer())).toBundle())
                .createPendingIntent()

        val name = child.name
        val contentText = name?.fold(
                ifLeft = {
                    getString(R.string.publishing_child_data_with_name, it.value.trim())
                }, ifRight = {
            getString(R.string.publishing_child_data_with_name, "${it.first.value} ${it.last.value}".trim())
        }
        ) ?: getString(R.string.publishing_child_data)

        return NotificationCompat.Builder(applicationContext, backgroundContextChannelId(applicationContext))
                .setContentTitle(getString(R.string.publishing))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_sherlock)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.publishing))
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .build()
    }

    private fun showPublishedSuccessfullyNotification(child: SimpleRetrievedChild?) {

        val pendingIntent = child?.let {
            NavDeepLinkBuilder(applicationContext)
                    .setGraph(R.navigation.app_nav_graph)
                    .setDestination(R.id.childDetailsFragment)
                    .setComponentName(MainActivity::class.java)
                    .setArguments(ChildDetailsFragmentArgs(it.id.bundle(ChildId.serializer())).toBundle())
                    .createPendingIntent()
        }

        val name = child?.name
        val contentText = name?.fold(
                ifLeft = {
                    getString(R.string.published_child_data_successfully_with_name, it.value.trim())
                }, ifRight = {
            getString(R.string.published_child_data_successfully_with_name, "${it.first.value} ${it.last.value}".trim())
        }
        ) ?: getString(R.string.published_child_data_successfully)

        val notification = NotificationCompat.Builder(applicationContext, backgroundContextChannelId(applicationContext))
                .setContentTitle(getString(R.string.success))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_sherlock)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.published_successfully))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .build()

        checkNotNull(ContextCompat.getSystemService(applicationContext, NotificationManager::class.java))
                .notify(NOTIFICATION_ID_PUBLISHED_SUCCESSFULLY, notification)
    }

    private fun showPublishingFailedNotification(throwable: Throwable, child: AppPublishedChild, isRecoverable: Boolean) {

        val name = child.name
        val contentTitle = name?.fold(
                ifLeft = { getString(R.string.publishing_failed_with_name, it.value.trim()) },
                ifRight = { getString(R.string.publishing_failed_with_name, "${it.first.value} ${it.last.value}".trim()) }
        ) ?: getString(R.string.publishing_failed)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, backgroundContextChannelId(applicationContext))
                .setContentTitle(contentTitle)
                .setSmallIcon(R.drawable.ic_sherlock)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setTicker(getString(R.string.publishing_failed))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))

        if (isRecoverable) {

            val pendingIntent = createIntent(child).let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(
                            applicationContext,
                            REQUEST_CODE_PUBLISHING_FAILED,
                            it,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
                } else {
                    PendingIntent.getService(
                            applicationContext,
                            REQUEST_CODE_PUBLISHING_FAILED,
                            it,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            }

            notificationBuilder.setContentText(getString(R.string.click_to_retry_with_reason, throwable.localizedMessage))
                    .setContentIntent(pendingIntent)
        } else {
            notificationBuilder.setContentText(throwable.localizedMessage)
        }

        checkNotNull(ContextCompat.getSystemService(applicationContext, NotificationManager::class.java))
                .notify(NOTIFICATION_ID_PUBLISHING_FAILED, notificationBuilder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        addChildDisposable?.dispose()
        super.onDestroy()
    }

    companion object {

        const val ACTION_PUBLISH_CHILD =
                "dev.ahmedmourad.sherlock.android.services.action.PUBLISH_CHILd"
        const val EXTRA_CHILD =
                "dev.ahmedmourad.sherlock.android.services.extra.CHILD"
        const val REQUEST_CODE_PUBLISHING_FAILED = 2427
        const val NOTIFICATION_ID_PUBLISH_CHILD = 7542
        const val NOTIFICATION_ID_PUBLISHED_SUCCESSFULLY = 1427
        const val NOTIFICATION_ID_PUBLISHING_FAILED = 3675

        fun createIntent(child: AppPublishedChild) = Intent(appCtx, SherlockService::class.java).apply {
            action = ACTION_PUBLISH_CHILD
            putExtra(EXTRA_CHILD, child.bundle(AppPublishedChild.serializer()))
        }
    }
}