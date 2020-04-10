package inc.ahmedmourad.sherlock.dagger.modules.factories

import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.adapters.AppSectionsRecyclerAdapter
import inc.ahmedmourad.sherlock.adapters.ChildrenRecyclerAdapter
import inc.ahmedmourad.sherlock.adapters.DynamicRecyclerAdapter
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.model.common.AppSection
import inc.ahmedmourad.sherlock.utils.formatter.Formatter

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
