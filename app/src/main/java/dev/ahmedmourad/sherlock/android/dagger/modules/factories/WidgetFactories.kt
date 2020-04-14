package dev.ahmedmourad.sherlock.android.dagger.modules.factories

import android.content.Context
import android.content.Intent
import android.widget.RemoteViewsService
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsFactory
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsService
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager

internal typealias ChildrenRemoteViewsServiceIntentFactory =
        (@JvmSuppressWildcards Int, @JvmSuppressWildcards Map<SimpleRetrievedChild, Weight>)
        -> @JvmSuppressWildcards Intent

internal fun childrenRemoteViewsServiceIntentFactory(
        appWidgetId: Int,
        results: Map<SimpleRetrievedChild, Weight>
): Intent {
    return ChildrenRemoteViewsService.create(appWidgetId, results)
}

internal typealias ChildrenRemoteViewsFactoryFactory =
        (@JvmSuppressWildcards Context, @JvmSuppressWildcards Map<SimpleRetrievedChild, Weight>)
        -> @JvmSuppressWildcards RemoteViewsService.RemoteViewsFactory

internal fun childrenRemoteViewsFactoryFactory(
        formatter: Lazy<Formatter>,
        dateManager: Lazy<DateManager>,
        context: Context,
        results: Map<SimpleRetrievedChild, Weight>
): RemoteViewsService.RemoteViewsFactory {
    return ChildrenRemoteViewsFactory(
            context,
            results,
            formatter,
            dateManager
    )
}
