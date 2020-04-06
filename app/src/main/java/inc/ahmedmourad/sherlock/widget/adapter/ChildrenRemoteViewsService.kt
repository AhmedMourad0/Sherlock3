package inc.ahmedmourad.sherlock.widget.adapter

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViewsService
import arrow.core.Tuple2
import arrow.core.toMap
import arrow.core.toTuple2
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRemoteViewsFactoryFactory
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.model.common.ParcelableWrapper
import inc.ahmedmourad.sherlock.model.common.parcelize
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

internal class ChildrenRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var childrenRemoteViewsFactoryFactory: ChildrenRemoteViewsFactoryFactory

    override fun onCreate() {
        super.onCreate()
        findAppComponent().plusChildrenRemoteViewsServiceComponent().inject(this)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {

        val hackBundle = requireNotNull(intent.getBundleExtra(EXTRA_HACK_BUNDLE))

        val children = requireNotNull(
                hackBundle.getParcelableArrayList<ParcelableWrapper<SimpleRetrievedChild>>(EXTRA_CHILDREN)
        ).map(ParcelableWrapper<SimpleRetrievedChild>::value)

        val weights = requireNotNull(
                hackBundle.getParcelableArrayList<ParcelableWrapper<Weight>>(EXTRA_WEIGHT)
        ).map(ParcelableWrapper<Weight>::value)

        require(children.size == weights.size)

        return childrenRemoteViewsFactoryFactory(
                applicationContext,
                children.zip(weights).map(Pair<SimpleRetrievedChild, Weight>::toTuple2)
        )
    }

    companion object {

        /** This's as ridiculous as it looks, but it's the only way this works */
        const val EXTRA_HACK_BUNDLE = "inc.ahmedmourad.sherlock.external.adapter.extra.HACK_BUNDLE"
        const val EXTRA_CHILDREN = "inc.ahmedmourad.sherlock.external.adapter.extra.CHILDREN"
        const val EXTRA_WEIGHT = "inc.ahmedmourad.sherlock.external.adapter.extra.WEIGHT"

        fun create(appWidgetId: Int, results: List<Tuple2<SimpleRetrievedChild, Weight>>): Intent {

            val resultsMap = results.toMap()
            val hackBundle = Bundle(2).apply {
                putParcelableArrayList(EXTRA_CHILDREN, ArrayList(resultsMap.keys.map(SimpleRetrievedChild::parcelize)))
                putParcelableArrayList(EXTRA_WEIGHT, ArrayList(resultsMap.values.map(Weight::parcelize)))
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
