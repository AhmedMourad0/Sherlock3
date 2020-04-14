package dev.ahmedmourad.sherlock.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import arrow.core.toT
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.ItemResultBinding
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import splitties.init.appCtx
import java.util.*

internal class ChildrenRecyclerAdapter(
        private val dateManager: Lazy<DateManager>,
        private val formatter: Lazy<Formatter>,
        private val onResultSelectedListener: (Tuple2<SimpleRetrievedChild, Weight>) -> Unit
) : DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, ChildrenRecyclerAdapter.ViewHolder>() {

    private val resultsList = ArrayList<Tuple2<SimpleRetrievedChild, Weight>>()

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(container.context).inflate(R.layout.item_result, container, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(resultsList[position])

    override fun getItemCount() = resultsList.size

    override fun update(items: Map<SimpleRetrievedChild, Weight>) {
        resultsList.clear()
        resultsList.addAll(items.entries.sortedByDescending { it.value.value }.map { it.key toT it.value })
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemResultBinding = ItemResultBinding.bind(view)
        private val glide: RequestManager = Glide.with(appCtx)

        internal fun bind(result: Tuple2<SimpleRetrievedChild, Weight>) {

            glide.load(result.a.pictureUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(binding.pictureImageView)

            //TODO: this needs to change with time
            binding.dateTextView.text = dateManager.get().getRelativeDateTimeString(result.a.publicationDate)
            binding.notesTextView.text = formatter.get().formatNotes(result.a.notes)
            binding.locationTextView.text = formatter.get().formatLocation(result.a.locationName, result.a.locationAddress)

            itemView.setOnClickListener { onResultSelectedListener(result) }
        }
    }
}
