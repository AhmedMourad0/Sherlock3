package inc.ahmedmourad.sherlock.view.controllers.children

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.adapters.DynamicRecyclerAdapter
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildDetailsControllerFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.model.common.ParcelableWrapper
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.model.common.parcelize
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.ChildrenSearchResultsViewModel
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.factories.ChildrenSearchResultsViewModelFactoryFactory
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ChildrenSearchResultsController(args: Bundle) : LifecycleController(args) {

    @BindView(R.id.search_results_recycler)
    internal lateinit var recyclerView: RecyclerView

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    internal lateinit var formatter: Lazy<Formatter>

    @Inject
    internal lateinit var adapterFactory: ChildrenRecyclerAdapterFactory

    @Inject
    internal lateinit var childDetailsControllerFactory: ChildDetailsControllerFactory

    @Inject
    internal lateinit var viewModelFactoryFactory: ChildrenSearchResultsViewModelFactoryFactory

    private lateinit var context: Context

    private lateinit var query: ChildQuery

    private lateinit var adapter: DynamicRecyclerAdapter<List<Tuple2<SimpleRetrievedChild, Weight>>, *>

    private lateinit var viewModel: ChildrenSearchResultsViewModel

    private var findAllResultsDisposable by disposable()

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.childrenSearchResultsComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_search_results, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        query = requireNotNull(args.getParcelable<ParcelableWrapper<ChildQuery>>(ARG_QUERY)).value

        initializeRecyclerView()

        viewModel = viewModelProvider(viewModelFactoryFactory(query))[ChildrenSearchResultsViewModel::class.java]

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        //TODO: either give the option to update or not, or onPublish new values to the bottom
        //TODO: paginate
        findAllResultsDisposable = viewModel.searchResultsFlowable.subscribe({ resultsEither ->
            resultsEither.fold(ifLeft = {
                Timber.error(it, it::toString)
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }, ifRight = adapter::update)
        }, {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        })
    }

    override fun onDetach(view: View) {
        findAllResultsDisposable?.dispose()
        super.onDetach(view)
    }

    private fun initializeRecyclerView() {

        adapter = adapterFactory {
            val taggedController = childDetailsControllerFactory(it.a)
            router.pushController(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.isVerticalScrollBarEnabled = true
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.childrenSearchResultsComponent.release()
        findAllResultsDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.ChildrenSearchResultsController"

        private const val ARG_QUERY = "inc.ahmedmourad.sherlock.view.controllers.arg.QUERY"

        fun newInstance(query: ChildQuery) = TaggedController(
                ChildrenSearchResultsController(Bundle(1).apply {
                    putParcelable(ARG_QUERY, query.parcelize())
                }), CONTROLLER_TAG
        )
    }
}
