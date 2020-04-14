package inc.ahmedmourad.sherlock.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.adapters.DynamicRecyclerAdapter
import inc.ahmedmourad.sherlock.bundlizer.bundle
import inc.ahmedmourad.sherlock.bundlizer.unbundle
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.databinding.FragmentChildrenSearchResultsBinding
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.ChildrenSearchResultsViewModel
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.ChildrenSearchResultsViewModelFactoryFactory
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

    private lateinit var adapter: DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *>

    private val viewModel: ChildrenSearchResultsViewModel by viewModels {
        viewModelFactoryFactory(this, args.query.unbundle(ChildQuery.serializer()))
    }

    private val args: ChildrenSearchResultsFragmentArgs by navArgs()
    private var binding: FragmentChildrenSearchResultsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusChildrenSearchResultsFragmentComponent().inject(this)

        //TODO: either give the option to update or not, or onPublish new values to the bottom
        //TODO: paginate
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { resultsEither ->
            resultsEither.fold(ifLeft = {
                Timber.error(it, it::toString)
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }, ifRight = adapter::update)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChildrenSearchResultsBinding.bind(view)
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        binding?.let { b ->

            adapter = adapterFactory {
                findNavController().navigate(
                        ChildrenSearchResultsFragmentDirections
                                .actionChildrenSearchResultsFragmentToChildDetailsFragment(it.a.bundle(SimpleRetrievedChild.serializer()))
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
