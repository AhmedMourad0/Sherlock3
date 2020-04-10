package inc.ahmedmourad.sherlock.widget.adapter

import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import arrow.core.toT
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error

internal class ChildrenRemoteViewsFactory(
        private val context: Context,
        results: Map<SimpleRetrievedChild, Weight>,
        private val formatter: Lazy<Formatter>,
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
                formatter.get().formatLocation(
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
