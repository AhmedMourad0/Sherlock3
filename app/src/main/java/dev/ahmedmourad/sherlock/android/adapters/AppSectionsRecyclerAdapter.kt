package dev.ahmedmourad.sherlock.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.ItemSectionBinding
import dev.ahmedmourad.sherlock.android.model.common.AppSection
import javax.inject.Inject

internal typealias OnSectionSelectedListener = (NavDirections?) -> Unit

internal class AppSectionsRecyclerAdapter(
        private val items: List<AppSection>,
        private val onSectionSelectedListener: OnSectionSelectedListener
) : RecyclerView.Adapter<AppSectionsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(container.context).inflate(R.layout.item_section, container, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemSectionBinding = ItemSectionBinding.bind(view)

        internal fun bind(item: AppSection) {
            binding.nameTextView.text = item.name
            binding.imageView.setImageResource(item.imageDrawable)
            itemView.setOnClickListener { onSectionSelectedListener(item.navDirectionFactory?.invoke()) }
        }
    }
}

internal interface AppSectionsRecyclerAdapterFactory :
        (List<AppSection>, OnSectionSelectedListener) -> RecyclerView.Adapter<*>

@Reusable
internal class AppSectionsRecyclerAdapterFactoryImpl @Inject constructor() :
        AppSectionsRecyclerAdapterFactory {
    override fun invoke(
            sectionsList: List<AppSection>,
            onSectionSelectedListener: OnSectionSelectedListener
    ): RecyclerView.Adapter<*> {
        return AppSectionsRecyclerAdapter(sectionsList, onSectionSelectedListener)
    }
}
