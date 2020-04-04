package inc.ahmedmourad.sherlock.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.textview.MaterialTextView
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.model.common.AppSection

internal class AppSectionsRecyclerAdapter(
        private val sectionsList: List<AppSection>,
        private val onSectionSelectedListener: (NavDirections?) -> Unit
) : RecyclerView.Adapter<AppSectionsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(container.context).inflate(R.layout.item_section, container, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(sectionsList[position])

    override fun getItemCount() = sectionsList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.section_name)
        internal lateinit var nameTextView: MaterialTextView

        @BindView(R.id.section_image)
        internal lateinit var imageView: ImageView

        init {
            ButterKnife.bind(this, view)
        }

        internal fun bind(section: AppSection) {
            nameTextView.text = section.name
            imageView.setImageResource(section.imageDrawable)
            itemView.setOnClickListener { onSectionSelectedListener(section.navDirectionFactory?.invoke()) }
        }
    }
}
