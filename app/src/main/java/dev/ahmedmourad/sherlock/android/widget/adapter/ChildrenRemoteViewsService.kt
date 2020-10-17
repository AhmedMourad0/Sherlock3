package dev.ahmedmourad.sherlock.android.widget.adapter

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViewsService
import dagger.Reusable
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import kotlinx.serialization.builtins.PairSerializer
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

internal class ChildrenRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var childrenRemoteViewsFactoryFactory: ChildrenRemoteViewsFactoryFactory

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {

        val children = intent.getBundleExtra(EXTRA_HACK_BUNDLE)
                ?.getParcelableArrayList<Bundle>(EXTRA_CHILDREN)
                ?.map {
                    it.unbundle(PairSerializer(SimpleRetrievedChild.serializer(), Weight.serializer()))
                }?.toMap() ?: emptyMap()

        return childrenRemoteViewsFactoryFactory(
                applicationContext,
                children
        )
    }

    companion object {

        /** This's as ridiculous as it looks, but it's the only way this works */
        const val EXTRA_HACK_BUNDLE =
                "dev.ahmedmourad.sherlock.android.widget.adapter.extra.HACK_BUNDLE"
        const val EXTRA_CHILDREN =
                "dev.ahmedmourad.sherlock.android.widget.adapter.extra.CHILDREN"

        fun create(appWidgetId: Int, results: Map<SimpleRetrievedChild, Weight>): Intent {

            val hackBundle = Bundle(1).apply {
                putParcelableArrayList(EXTRA_CHILDREN,
                        ArrayList(results.entries.map {
                            it.toPair().bundle(PairSerializer(SimpleRetrievedChild.serializer(), Weight.serializer()))
                        })
                )
            }

            return Intent(appCtx, ChildrenRemoteViewsService::class.java).also { intent ->
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent.data = getUniqueDataUri(appWidgetId)
                intent.putExtra(EXTRA_HACK_BUNDLE, hackBundle)
            }
        }

        private fun getUniqueDataUri(appWidgetId: Int): Uri {
            return Uri.withAppendedPath(Uri.parse("sherlock://widget/id/"), "$appWidgetId${UUID.randomUUID()}")
        }
    }
}

internal interface ChildrenRemoteViewsServiceIntentFactory :
        (Int, Map<SimpleRetrievedChild, Weight>) -> Intent

@Reusable
internal class ChildrenRemoteViewsServiceIntentFactoryImpl @Inject constructor() :
        ChildrenRemoteViewsServiceIntentFactory {
    override fun invoke(appWidgetId: Int, results: Map<SimpleRetrievedChild, Weight>): Intent {
        return ChildrenRemoteViewsService.create(appWidgetId, results)
    }
}
