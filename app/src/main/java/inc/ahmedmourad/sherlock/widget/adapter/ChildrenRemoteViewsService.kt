package inc.ahmedmourad.sherlock.widget.adapter

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViewsService
import inc.ahmedmourad.sherlock.bundlizer.bundle
import inc.ahmedmourad.sherlock.bundlizer.unbundle
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRemoteViewsFactoryFactory
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import kotlinx.serialization.builtins.MapSerializer
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject

internal class ChildrenRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var childrenRemoteViewsFactoryFactory: ChildrenRemoteViewsFactoryFactory

    override fun onCreate() {
        super.onCreate()
        findAppComponent().plusChildrenRemoteViewsServiceComponent().inject(this)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {

        val children = requireNotNull(
                intent.getBundleExtra(EXTRA_HACK_BUNDLE)?.getBundle(EXTRA_CHILDREN_RESULTS)
        ).unbundle(MapSerializer(SimpleRetrievedChild.serializer(), Weight.serializer()))

        return childrenRemoteViewsFactoryFactory(
                applicationContext,
                children
        )
    }

    companion object {

        /** This's as ridiculous as it looks, but it's the only way this works */
        const val EXTRA_HACK_BUNDLE =
                "inc.ahmedmourad.sherlock.widget.adapter.extra.HACK_BUNDLE"
        const val EXTRA_CHILDREN_RESULTS =
                "inc.ahmedmourad.sherlock.widget.adapter.extra.CHILDREN_RESULTS"

        fun create(appWidgetId: Int, results: Map<SimpleRetrievedChild, Weight>): Intent {

            val resultsMap = results
            val hackBundle = Bundle(1).apply {
                putBundle(
                        EXTRA_CHILDREN_RESULTS,
                        resultsMap.bundle(MapSerializer(SimpleRetrievedChild.serializer(), Weight.serializer()))
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
