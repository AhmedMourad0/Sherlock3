package dev.ahmedmourad.sherlock.android.di.modules.factories

import androidx.recyclerview.widget.RecyclerView
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.adapters.*
import dev.ahmedmourad.sherlock.android.model.common.AppSection
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import javax.inject.Inject

internal interface ChildrenRecyclerAdapterFactory :
        (OnChildSelectedListener) -> DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *>

@Reusable
internal class ChildrenRecyclerAdapterFactoryImpl @Inject constructor(
        private val dateManager: Lazy<DateManager>,
        private val textFormatter: Lazy<TextFormatter>
) : ChildrenRecyclerAdapterFactory {
    override fun invoke(onChildSelectedListener: OnChildSelectedListener): DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *> {
        return ChildrenRecyclerAdapter(dateManager, textFormatter, onChildSelectedListener)
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
