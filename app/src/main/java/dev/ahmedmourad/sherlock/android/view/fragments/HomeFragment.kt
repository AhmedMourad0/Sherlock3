package dev.ahmedmourad.sherlock.android.view.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import arrow.syntax.function.partially1
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentHomeBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.di.modules.factories.AppSectionsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.model.common.AppSection
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject

internal class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    internal lateinit var adapterFactory: AppSectionsRecyclerAdapterFactory

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
                HomeFragmentDirections.Companion::actionHomeFragmentToAddChildFragment.partially1(null)
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
