package inc.ahmedmourad.sherlock.dagger.modules.factories

import android.content.Context
import android.content.Intent
import android.widget.RemoteViewsService
import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.widget.adapter.ChildrenRemoteViewsFactory
import inc.ahmedmourad.sherlock.widget.adapter.ChildrenRemoteViewsService

internal typealias ChildrenRemoteViewsServiceIntentFactory =
        (@JvmSuppressWildcards Int, @JvmSuppressWildcards List<Tuple2<SimpleRetrievedChild, Weight>>)
        -> @JvmSuppressWildcards Intent

internal fun childrenRemoteViewsServiceIntentFactory(
        appWidgetId: Int,
        results: List<Tuple2<SimpleRetrievedChild, Weight>>
): Intent {
    return ChildrenRemoteViewsService.create(appWidgetId, results)
}

internal typealias ChildrenRemoteViewsFactoryFactory =
        (@JvmSuppressWildcards Context, @JvmSuppressWildcards List<Tuple2<SimpleRetrievedChild, Weight>>)
        -> @JvmSuppressWildcards RemoteViewsService.RemoteViewsFactory

internal fun childrenRemoteViewsFactoryFactory(
        formatter: Lazy<Formatter>,
        dateManager: Lazy<DateManager>,
        context: Context,
        results: List<Tuple2<SimpleRetrievedChild, Weight>>
): RemoteViewsService.RemoteViewsFactory {
    return ChildrenRemoteViewsFactory(
            context,
            results,
            formatter,
            dateManager
    )
}
