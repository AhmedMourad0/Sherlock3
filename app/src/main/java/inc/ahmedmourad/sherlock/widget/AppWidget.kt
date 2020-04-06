package inc.ahmedmourad.sherlock.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import arrow.core.Either
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRemoteViewsServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.interactors.children.FindLastSearchResultsInteractor
import inc.ahmedmourad.sherlock.utils.DisposablesSparseArray
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class AppWidget : AppWidgetProvider() {

    @Inject
    lateinit var interactor: FindLastSearchResultsInteractor

    @Inject
    lateinit var childrenRemoteViewsServiceFactory: ChildrenRemoteViewsServiceIntentFactory

    private val disposables = DisposablesSparseArray()

    init {
        appCtx.findAppComponent().plusAppWidgetComponent().inject(this)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds)
            disposables.put(appWidgetId, updateAppWidget(context, appWidgetManager, appWidgetId))
    }

    /**
     * Used to update the ui of a certain widget
     *
     * @param context          The Context in which this receiver is running.
     * @param appWidgetManager A AppWidgetManager object you can call AppWidgetManager.updateAppWidget on.
     * @param appWidgetId      The appWidgetIds for which an update is needed.
     */
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int): Disposable {
        return interactor()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->

                    val views = RemoteViews(context.packageName, R.layout.app_widget)

                    views.setImageViewResource(R.id.widget_icon, R.drawable.ic_sherlock)

                    views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)

                    when (either) {

                        is Either.Left -> {
                            Timber.error(either.a, either.a::toString)
                        }

                        is Either.Right -> {
                            views.setRemoteAdapter(R.id.widget_list_view,
                                    childrenRemoteViewsServiceFactory(appWidgetId, either.b)
                            ) //TODO: show retry view
                        }
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)

                }, {
                    Timber.error(it, it::toString)
                }) //TODO: show retry view
    }

    private fun retry(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        disposables.dispose(appWidgetIds)
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        disposables.dispose()
        super.onDisabled(context)
    }
}
