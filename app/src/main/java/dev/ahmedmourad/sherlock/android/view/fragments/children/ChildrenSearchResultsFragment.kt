package dev.ahmedmourad.sherlock.android.view.fragments.children

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
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.adapters.ChildrenRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.adapters.DynamicRecyclerAdapter
import dev.ahmedmourad.sherlock.android.databinding.FragmentChildrenSearchResultsBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ChildrenSearchResultsViewModel
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import javax.inject.Inject
import javax.inject.Provider

internal class ChildrenSearchResultsFragment : Fragment(R.layout.fragment_children_search_results) {

    @Inject
    internal lateinit var adapterFactory: ChildrenRecyclerAdapterFactory

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<ChildrenSearchResultsViewModel>>

    private lateinit var adapter: DynamicRecyclerAdapter<Map<SimpleRetrievedChild, Weight>, *>

    private val viewModel: ChildrenSearchResultsViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                ChildrenSearchResultsViewModel.defaultArgs(args.query.unbundle(ChildrenQuery.serializer()))
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
        observe(viewModel.state, Observer { state ->
            when (state) {

                is ChildrenSearchResultsViewModel.State.Data -> {
                    adapter.update(state.items)
                    binding?.let { b ->
                        b.recycler.visibility = View.VISIBLE
                        b.startInvestigation.show()
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                    }
                }

                ChildrenSearchResultsViewModel.State.NoData -> {
                    adapter.update(emptyMap())
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.startInvestigation.hide()
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.have_no_ongoing_investigations)
                        b.error.errorIcon.setImageResource(R.drawable.ic_detective_woman)
                    }
                }

                ChildrenSearchResultsViewModel.State.Loading -> {
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.startInvestigation.hide()
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.VISIBLE
                    }
                }

                ChildrenSearchResultsViewModel.State.NoInternet -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.startInvestigation.hide()
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.no_internet_connection)
                        b.error.errorIcon.setImageResource(R.drawable.ic_no_internet_colorful)
                    }
                }

                ChildrenSearchResultsViewModel.State.NoSignedInUser -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.startInvestigation.hide()
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.sign_in_needed_to_view)
                        b.error.errorIcon.setImageResource(R.drawable.ic_finger_print)
                    }
                    Toast.makeText(
                            appCtx,
                            R.string.authentication_needed,
                            Toast.LENGTH_LONG
                    ).show()
                }

                ChildrenSearchResultsViewModel.State.Error -> {
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.startInvestigation.hide()
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.something_went_wrong)
                        b.error.errorIcon.setImageResource(R.drawable.ic_research)
                    }
                }
            }.exhaust()
        })

        observe(viewModel.startInvestigationSuccess, Observer {
            binding?.startInvestigation?.hide()
            Toast.makeText(
                    appCtx,
                    getString(R.string.start_investigation_success),
                    Toast.LENGTH_LONG
            ).show()
            viewModel.onStartInvestigationSuccessHandled()
        })

        observe(viewModel.startInvestigationError, Observer { msg ->
            if (msg != null) {
                Toast.makeText(
                        appCtx,
                        msg,
                        Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                        appCtx,
                        R.string.something_went_wrong,
                        Toast.LENGTH_LONG
                ).show()
            }
            viewModel.onStartInvestigationErrorHandled()
            binding?.startInvestigation?.isEnabled = true
        })

        binding?.startInvestigation?.setOnClickListener {
            viewModel.onStartInvestigation()
            binding?.startInvestigation?.isEnabled = false
        }

        binding?.error?.root?.setOnClickListener {
            viewModel.onRefresh()
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

    override fun onStart() {
        super.onStart()
        viewModel.onRefresh()
    }

    override fun onStop() {
        viewModel.onInvalidateAllQueries()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
