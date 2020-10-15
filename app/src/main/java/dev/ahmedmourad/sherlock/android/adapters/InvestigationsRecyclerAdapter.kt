package dev.ahmedmourad.sherlock.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import arrow.core.right
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.ItemInvestigationBinding
import dev.ahmedmourad.sherlock.android.formatter.TextFormatter
import dev.ahmedmourad.sherlock.domain.model.children.Investigation
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import java.util.*
import javax.inject.Inject

internal typealias OnInvestigationSelectedListener = (Investigation) -> Unit

internal class InvestigationsRecyclerAdapter(
        private val dateManager: Lazy<DateManager>,
        private val textFormatter: Lazy<TextFormatter>,
        private val onInvestigationSelectedListener: OnInvestigationSelectedListener
) : DynamicRecyclerAdapter<List<Investigation>, InvestigationsRecyclerAdapter.ViewHolder>() {

    private val items = ArrayList<Investigation>()

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(container.context).inflate(R.layout.item_investigation, container, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    override fun update(items: List<Investigation>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemInvestigationBinding = ItemInvestigationBinding.bind(view)

        internal fun bind(item: Investigation) {
            binding.childName.text = textFormatter.get().formatName(item.fullName.right())
            binding.timestamp.text = dateManager.get().getRelativeDateTimeString(item.timestamp)
            itemView.setOnClickListener { onInvestigationSelectedListener(item) }
        }
    }
}

internal interface InvestigationsRecyclerAdapterFactory :
        (OnInvestigationSelectedListener) -> DynamicRecyclerAdapter<List<Investigation>, *>

@Reusable
internal class InvestigationsRecyclerAdapterFactoryImpl @Inject constructor(
        private val dateManager: Lazy<DateManager>,
        private val textFormatter: Lazy<TextFormatter>
) : InvestigationsRecyclerAdapterFactory {
    override fun invoke(
            onInvestigationSelectedListener: OnInvestigationSelectedListener
    ): DynamicRecyclerAdapter<List<Investigation>, *> {
        return InvestigationsRecyclerAdapter(
                dateManager,
                textFormatter,
                onInvestigationSelectedListener
        )
    }
}
