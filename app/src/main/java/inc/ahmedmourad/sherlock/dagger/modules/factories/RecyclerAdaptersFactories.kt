package inc.ahmedmourad.sherlock.dagger.modules.factories

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
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.formatter.Formatter

private typealias OnChildClickListener = (Tuple2<SimpleRetrievedChild, Weight>) -> Unit

internal typealias ChildrenRecyclerAdapterFactory =
        (@JvmSuppressWildcards OnChildClickListener) ->
        @JvmSuppressWildcards DynamicRecyclerAdapter<List<Tuple2<SimpleRetrievedChild, Weight>>, *>

internal fun childrenRecyclerAdapterFactory(
        dateManager: Lazy<DateManager>,
        formatter: Lazy<Formatter>,
        onResultSelectedListener: (Tuple2<SimpleRetrievedChild, Weight>) -> Unit
): DynamicRecyclerAdapter<List<Tuple2<SimpleRetrievedChild, Weight>>, *> {
    return ChildrenRecyclerAdapter(dateManager, formatter, onResultSelectedListener)
}

private typealias OnSectionClickListener = (Lazy<out TaggedController>?) -> Unit

internal typealias AppSectionsRecyclerAdapterFactory =
        (@JvmSuppressWildcards List<AppSection>, @JvmSuppressWildcards OnSectionClickListener) ->
        @JvmSuppressWildcards RecyclerView.Adapter<*>

internal fun appSectionsRecyclerAdapterFactory(
        sectionsList: List<AppSection>,
        onSectionSelectedListener: (Lazy<out TaggedController>?) -> Unit
): RecyclerView.Adapter<*> {
    return AppSectionsRecyclerAdapter(sectionsList, onSectionSelectedListener)
}
