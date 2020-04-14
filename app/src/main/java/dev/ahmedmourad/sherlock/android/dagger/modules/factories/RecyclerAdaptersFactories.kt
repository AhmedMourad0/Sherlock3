package dev.ahmedmourad.sherlock.android.dagger.modules.factories

import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.adapters.AppSectionsRecyclerAdapter
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapter
import dev.ahmedmourad.sherlock.android.adapters.DynamicRecyclerAdapter
import dev.ahmedmourad.sherlock.android.model.common.AppSection
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager

private typealias OnChildClickListener = (Tuple2<SimpleRetrievedChild, Weight>) -> Unit

internal typealias ChildrenRecyclerAdapterFactory =
        (@JvmSuppressWildcards OnChildClickListener) ->
        @JvmSuppressWildcards DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *>

internal fun childrenRecyclerAdapterFactory(
        dateManager: Lazy<DateManager>,
        formatter: Lazy<Formatter>,
        onResultSelectedListener: (Tuple2<SimpleRetrievedChild, Weight>) -> Unit
): DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *> {
    return ChildrenRecyclerAdapter(dateManager, formatter, onResultSelectedListener)
}

private typealias OnSectionClickListener = (NavDirections?) -> Unit

internal typealias AppSectionsRecyclerAdapterFactory =
        (@JvmSuppressWildcards List<AppSection>, @JvmSuppressWildcards OnSectionClickListener) ->
        @JvmSuppressWildcards RecyclerView.Adapter<*>

internal fun appSectionsRecyclerAdapterFactory(
        sectionsList: List<AppSection>,
        onSectionSelectedListener: (NavDirections?) -> Unit
): RecyclerView.Adapter<*> {
    return AppSectionsRecyclerAdapter(sectionsList, onSectionSelectedListener)
}
