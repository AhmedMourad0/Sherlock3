package dev.ahmedmourad.sherlock.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import arrow.core.toT
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.ItemResultBinding
import dev.ahmedmourad.sherlock.android.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import java.util.*
import javax.inject.Inject

internal typealias OnChildSelectedListener = (Tuple2<SimpleRetrievedChild, Weight>) -> Unit

internal class ChildrenRecyclerAdapter(
        private val dateManager: Lazy<DateManager>,
        private val textFormatter: Lazy<TextFormatter>,
        private val imageLoader: Lazy<ImageLoader>,
        private val onChildSelectedListener: OnChildSelectedListener
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

        internal fun bind(result: Tuple2<SimpleRetrievedChild, Weight>) {

            imageLoader.get().load(
                    result.a.pictureUrl?.value,
                    binding.childPicture,
                    R.drawable.placeholder,
                    R.drawable.placeholder
            )

            imageLoader.get().load(
                    result.a.user.pictureUrl?.value,
                    binding.userProfilePicture,
                    R.drawable.placeholder,
                    R.drawable.placeholder
            )

            binding.userDisplayName.text = textFormatter.get().formatDisplayName(result.a.user.displayName)
            //TODO: this needs to change with time
            binding.timestamp.text = dateManager.get().getRelativeDateTimeString(result.a.timestamp)
            binding.notes.text = textFormatter.get().formatNotes(result.a.notes)
            binding.location.text = textFormatter.get().formatLocation(result.a.locationName, result.a.locationAddress)

            itemView.setOnClickListener { onChildSelectedListener(result) }
        }
    }
}

internal interface ChildrenRecyclerAdapterFactory :
        (OnChildSelectedListener) -> DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *>

@Reusable
internal class ChildrenRecyclerAdapterFactoryImpl @Inject constructor(
        private val dateManager: Lazy<DateManager>,
        private val textFormatter: Lazy<TextFormatter>,
        private val imageLoader: Lazy<ImageLoader>
) : ChildrenRecyclerAdapterFactory {
    override fun invoke(
            onChildSelectedListener: OnChildSelectedListener
    ): DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *> {
        return ChildrenRecyclerAdapter(
                dateManager,
                textFormatter,
                imageLoader,
                onChildSelectedListener
        )
    }
}
