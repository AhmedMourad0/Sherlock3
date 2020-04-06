package inc.ahmedmourad.sherlock.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.adapters.DynamicRecyclerAdapter
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.databinding.FragmentChildrenSearchResultsBinding
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.ChildrenSearchResultsViewModel
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories.ChildrenSearchResultsViewModelFactoryFactory
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ChildrenSearchResultsFragment : Fragment(R.layout.fragment_children_search_results) {

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    internal lateinit var formatter: Lazy<Formatter>

    @Inject
    internal lateinit var adapterFactory: ChildrenRecyclerAdapterFactory

    @Inject
    internal lateinit var viewModelFactoryFactory: ChildrenSearchResultsViewModelFactoryFactory

    private lateinit var adapter: DynamicRecyclerAdapter<List<Tuple2<SimpleRetrievedChild, Weight>>, *>

    private lateinit var viewModel: ChildrenSearchResultsViewModel

    private var findAllResultsDisposable by disposable()

    private val args: ChildrenSearchResultsFragmentArgs by navArgs()
    private var binding: FragmentChildrenSearchResultsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusChildrenSearchResultsFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChildrenSearchResultsBinding.bind(view)
        initializeRecyclerView()
        viewModel = ViewModelProvider(this,
                viewModelFactoryFactory(args.query.unbundle(ChildQuery.serializer()))
        )[ChildrenSearchResultsViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
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

    override fun onStop() {
        findAllResultsDisposable?.dispose()
        super.onStop()
    }

    private fun initializeRecyclerView() {
        binding?.let { b ->

            adapter = adapterFactory {
                findNavController().navigate(
                        ChildrenSearchResultsFragmentDirections
                                .actionChildrenSearchResultsFragmentToChildDetailsFragment(it.a.bundle(ChildQuery.serializer()))
                )
            }

            b.recycler.adapter = adapter
            b.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            b.recycler.isVerticalScrollBarEnabled = true
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
