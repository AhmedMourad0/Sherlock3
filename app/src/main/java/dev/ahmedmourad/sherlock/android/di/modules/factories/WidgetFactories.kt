package dev.ahmedmourad.sherlock.android.di.modules.factories

import android.content.Context
import android.content.Intent
import android.widget.RemoteViewsService
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsFactory
import dev.ahmedmourad.sherlock.android.widget.adapter.ChildrenRemoteViewsService
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import javax.inject.Inject

internal interface ChildrenRemoteViewsServiceIntentFactory :
        (Int, Map<SimpleRetrievedChild, Weight>) -> Intent

@Reusable
internal class ChildrenRemoteViewsServiceIntentFactoryImpl @Inject constructor() :
        ChildrenRemoteViewsServiceIntentFactory {
    override fun invoke(appWidgetId: Int, results: Map<SimpleRetrievedChild, Weight>): Intent {
        return ChildrenRemoteViewsService.create(appWidgetId, results)
    }
}

internal interface ChildrenRemoteViewsFactoryFactory :
        (Context, Map<SimpleRetrievedChild, Weight>) -> RemoteViewsService.RemoteViewsFactory

@Reusable
internal class ChildrenRemoteViewsFactoryFactoryImpl @Inject constructor(
        private val textFormatter: Lazy<TextFormatter>,
        private val dateManager: Lazy<DateManager>
) : ChildrenRemoteViewsFactoryFactory {
    override fun invoke(
            context: Context,
            results: Map<SimpleRetrievedChild, Weight>
    ): RemoteViewsService.RemoteViewsFactory {
        return ChildrenRemoteViewsFactory(
                context,
                results,
                textFormatter,
                dateManager
        )
    }
}
