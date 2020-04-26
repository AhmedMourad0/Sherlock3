package dev.ahmedmourad.sherlock.android.widget.adapter

import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import arrow.core.toT
import com.bumptech.glide.Glide
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ChildrenRemoteViewsFactory(
        private val context: Context,
        results: Map<SimpleRetrievedChild, Weight>,
        private val textFormatter: Lazy<TextFormatter>,
        private val dateManager: Lazy<DateManager>
) : RemoteViewsService.RemoteViewsFactory {

    private val results = results.entries
            .sortedByDescending { it.value.value }
            .map { it.key toT it.value }

    override fun onCreate() {

    }

    override fun onDataSetChanged() {

    }

    override fun getCount() = results.size

    override fun getViewAt(position: Int): RemoteViews? {

        if (position >= count)
            return null

        val result = results[position]

        val views = RemoteViews(context.packageName, R.layout.item_widget_result)

        //TODO: needs to change over time
        views.setTextViewText(
                R.id.widget_result_date,
                dateManager.get().getRelativeDateTimeString(result.a.publicationDate)
        )

        views.setTextViewText(
                R.id.widget_result_notes,
                result.a.notes
        )

        views.setTextViewText(
                R.id.widget_result_location,
                textFormatter.get().formatLocation(
                        result.a.locationName,
                        result.a.locationAddress
                )
        )

        setPicture(views, result.a.pictureUrl)

        return views
    }

    private fun setPicture(views: RemoteViews, pictureUrl: Url?) {

        var bitmap: Bitmap?

        try {
            bitmap = Glide.with(appCtx)
                    .asBitmap()
                    .load(pictureUrl)
                    .submit()
                    .get()
        } catch (e: Exception) {
            bitmap = null
            Timber.error(e, e::toString)
        }

        if (bitmap != null)
            views.setImageViewBitmap(R.id.widget_result_picture, bitmap)
        else
            views.setImageViewResource(R.id.widget_result_picture, R.drawable.placeholder)
    }

    override fun getLoadingView() = null

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = false

    override fun onDestroy() {

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
