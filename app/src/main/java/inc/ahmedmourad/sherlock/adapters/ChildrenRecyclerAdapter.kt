package inc.ahmedmourad.sherlock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.textview.MaterialTextView
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import splitties.init.appCtx
import java.util.*

internal class ChildrenRecyclerAdapter(
        private val dateManager: Lazy<DateManager>,
        private val formatter: Lazy<Formatter>,
        private val onResultSelectedListener: (Tuple2<SimpleRetrievedChild, Weight>) -> Unit
) : DynamicRecyclerAdapter<List<Tuple2<SimpleRetrievedChild, Weight>>, ChildrenRecyclerAdapter.ViewHolder>() {

    private val resultsList = ArrayList<Tuple2<SimpleRetrievedChild, Weight>>()

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(container.context).inflate(R.layout.item_result, container, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(resultsList[position])

    override fun getItemCount() = resultsList.size

    override fun update(items: List<Tuple2<SimpleRetrievedChild, Weight>>) {
        resultsList.clear()
        resultsList.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.result_date)
        internal lateinit var dateTextView: MaterialTextView

        @BindView(R.id.result_picture)
        internal lateinit var pictureImageView: ImageView

        @BindView(R.id.result_notes)
        internal lateinit var notesTextView: MaterialTextView

        @BindView(R.id.result_location)
        internal lateinit var locationTextView: MaterialTextView

        private val glide: RequestManager

        init {
            ButterKnife.bind(this, view)
            glide = Glide.with(appCtx)
        }

        internal fun bind(result: Tuple2<SimpleRetrievedChild, Weight>) {

            glide.load(result.a.pictureUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(pictureImageView)

            //TODO: this needs to change with time
            dateTextView.text = dateManager.get().getRelativeDateTimeString(result.a.publicationDate)
            notesTextView.text = formatter.get().formatNotes(result.a.notes)
            locationTextView.text = formatter.get().formatLocation(result.a.locationName, result.a.locationAddress)

            itemView.setOnClickListener { onResultSelectedListener(result) }
        }
    }
}
