package dev.ahmedmourad.sherlock.android.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.adapters.DynamicRecyclerAdapter
import dev.ahmedmourad.sherlock.android.databinding.FragmentChildrenSearchResultsBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.interpreters.interactors.localizedMessage
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ChildrenSearchResultsViewModel
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class ChildrenSearchResultsFragment : Fragment(R.layout.fragment_children_search_results) {

    @Inject
    internal lateinit var dateManager: Lazy<DateManager>

    @Inject
    internal lateinit var textFormatter: Lazy<TextFormatter>

    @Inject
    internal lateinit var adapterFactory: ChildrenRecyclerAdapterFactory

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<ChildrenSearchResultsViewModel>>

    private lateinit var adapter: DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *>

    private val viewModel: ChildrenSearchResultsViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                ChildrenSearchResultsViewModel.defaultArgs(args.query.unbundle(ChildQuery.serializer()))
        )
    }

    private val args: ChildrenSearchResultsFragmentArgs by navArgs()
    private var binding: FragmentChildrenSearchResultsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChildrenSearchResultsBinding.bind(view)
        initializeRecyclerView()

        //TODO: either give the option to update or not, or onPublish new values to the bottom
        //TODO: paginate
        //TODO: show loading, hide when date is received
        observe(viewModel.searchResults) { resultsEither ->
            resultsEither.fold(ifLeft = { e ->
                //TODO: show error image with retry option when appropriate
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (e) {
                    FindChildrenInteractor.Exception.NoInternetConnectionException -> { /* do nothing*/
                    }
                    FindChildrenInteractor.Exception.NoSignedInUserException -> {
                        findNavController().popBackStack()
                    }
                    is FindChildrenInteractor.Exception.UnknownException -> {
                        Timber.error(message = e::toString)
                    }
                }.exhaust()
                Toast.makeText(context, e.localizedMessage(), Toast.LENGTH_LONG).show()
            }, ifRight = adapter::update)
        }
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
