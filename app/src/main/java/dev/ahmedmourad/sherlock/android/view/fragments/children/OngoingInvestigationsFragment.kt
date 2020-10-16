package dev.ahmedmourad.sherlock.android.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.adapters.DynamicRecyclerAdapter
import dev.ahmedmourad.sherlock.android.adapters.InvestigationsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.databinding.FragmentOngoingInvestigationsBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.OngoingInvestigationsViewModel
import dev.ahmedmourad.sherlock.domain.model.children.Investigation
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import javax.inject.Inject
import javax.inject.Provider

internal class OngoingInvestigationsFragment : Fragment(R.layout.fragment_ongoing_investigations) {

    @Inject
    internal lateinit var adapterFactory: InvestigationsRecyclerAdapterFactory

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<OngoingInvestigationsViewModel>>

    private lateinit var adapter: DynamicRecyclerAdapter<List<Investigation>, *>

    private val viewModel: OngoingInvestigationsViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                OngoingInvestigationsViewModel.defaultArgs()
        )
    }

    private var binding: FragmentOngoingInvestigationsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOngoingInvestigationsBinding.bind(view)
        (requireActivity() as BackdropActivity).setTitle(getString(R.string.ongoing_investigations))
        initializeRecyclerView()

        observe(viewModel.state, Observer { state ->
            when (state) {

                is OngoingInvestigationsViewModel.State.Data -> {
                    adapter.update(state.items)
                    binding?.let { b ->
                        b.recycler.visibility = View.VISIBLE
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                    }
                }

                OngoingInvestigationsViewModel.State.NoData -> {
                    adapter.update(emptyList())
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.have_no_ongoing_investigations)
                        b.error.errorIcon.setImageResource(R.drawable.ic_detective_woman)
                    }
                }

                OngoingInvestigationsViewModel.State.Loading -> {
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.VISIBLE
                    }
                }

                OngoingInvestigationsViewModel.State.NoInternet -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.no_internet_connection)
                        b.error.errorIcon.setImageResource(R.drawable.ic_no_internet_colorful)
                    }
                }

                OngoingInvestigationsViewModel.State.NoSignedInUser -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
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

                OngoingInvestigationsViewModel.State.Error -> {
                    binding?.let { b ->
                        b.recycler.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.something_went_wrong)
                        b.error.errorIcon.setImageResource(R.drawable.ic_research)
                    }
                }
            }.exhaust()
        })

        binding?.error?.root?.setOnClickListener {
            viewModel.onRefresh()
        }
    }

    private fun initializeRecyclerView() {
        binding?.let { b ->
            adapter = adapterFactory(viewModel::onInvestigationSelected)
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
