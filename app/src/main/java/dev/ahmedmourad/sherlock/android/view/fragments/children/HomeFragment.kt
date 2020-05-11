package dev.ahmedmourad.sherlock.android.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import arrow.syntax.function.partially1
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.adapters.AppSectionsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.databinding.FragmentHomeBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.model.children.AppPublishedChild
import dev.ahmedmourad.sherlock.android.model.common.AppSection
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.HomeViewModel
import kotlinx.serialization.builtins.nullable
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

internal class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<HomeViewModel>>

    @Inject
    internal lateinit var adapterFactory: AppSectionsRecyclerAdapterFactory

    private val viewModel: HomeViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                HomeViewModel.defaultArgs()
        )
    }

    private var binding: FragmentHomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        binding?.let { b ->
            b.recycler.adapter = adapterFactory(createSectionsList()) {
                if (it == null) {
                    Toast.makeText(appCtx, R.string.coming_soon, Toast.LENGTH_LONG).show()
                } else {
                    findNavController().navigate(it)
                }
            }
            b.recycler.layoutManager = GridLayoutManager(context, appCtx.resources.getInteger(R.integer.home_column_count))
            b.recycler.isVerticalScrollBarEnabled = true
        }
    }

    private fun createSectionsList() = ArrayList<AppSection>(4).apply {
        add(AppSection(
                appCtx.getString(R.string.found_a_child),
                R.drawable.found_a_child,
                HomeFragmentDirections.Companion::actionHomeFragmentToAddChildFragment.partially1(null.bundle(AppPublishedChild.serializer().nullable))
        ))
        add(AppSection(
                appCtx.getString(R.string.search),
                R.drawable.search_child,
                HomeFragmentDirections.Companion::actionHomeFragmentToFindChildrenFragment
        ))
        add(AppSection(appCtx.getString(R.string.coming_soon), R.drawable.coming_soon, null))
        add(AppSection(appCtx.getString(R.string.coming_soon), R.drawable.coming_soon, null))
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
