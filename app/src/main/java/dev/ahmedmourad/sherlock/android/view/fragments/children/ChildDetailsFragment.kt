package dev.ahmedmourad.sherlock.android.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import dagger.Lazy
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentChildDetailsBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ChildDetailsViewModel
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import javax.inject.Inject
import javax.inject.Provider

internal class ChildDetailsFragment : Fragment(R.layout.fragment_child_details) {

    @Inject
    internal lateinit var textFormatter: Lazy<TextFormatter>

    @Inject
    internal lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<ChildDetailsViewModel>>

    private val viewModel: ChildDetailsViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                ChildDetailsViewModel.defaultArgs(args.childId.unbundle(ChildId.serializer()))
        )
    }

    private val args: ChildDetailsFragmentArgs by navArgs()
    private var binding: FragmentChildDetailsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChildDetailsBinding.bind(view)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding?.toolbar)

        observe(viewModel.state, Observer { state ->
            when (state) {

                is ChildDetailsViewModel.State.Data -> {
                    populateUi(state.item)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.VISIBLE
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                    }
                }

                ChildDetailsViewModel.State.NoData -> {
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.child_data_missing)
                        b.error.errorIcon.setImageResource(R.drawable.ic_records)
                    }
                }

                ChildDetailsViewModel.State.Loading -> {
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.error.root.visibility = View.GONE
                        b.loading.root.visibility = View.VISIBLE
                    }
                }

                ChildDetailsViewModel.State.NoInternet -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
                        b.loading.root.visibility = View.GONE
                        b.error.root.visibility = View.VISIBLE
                        b.error.errorMessage.setText(R.string.no_internet_connection)
                        b.error.errorIcon.setImageResource(R.drawable.ic_no_internet_colorful)
                    }
                }

                ChildDetailsViewModel.State.NoSignedInUser -> {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
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

                ChildDetailsViewModel.State.Error -> {
                    binding?.let { b ->
                        b.contentRoot.visibility = View.GONE
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

    private fun populateUi(result: Pair<RetrievedChild, Weight?>) {

        binding?.let { b ->

            imageLoader.get().load(
                    result.first.pictureUrl?.value,
                    b.childPicture,
                    R.drawable.placeholder,
                    R.drawable.placeholder
            )

            val name = textFormatter.get().formatName(result.first.name)
            b.toolbar.title = name
            b.childName.text = name

            b.childAge.text = textFormatter.get().formatAge(result.first.appearance.ageRange)

            b.childGender.text = textFormatter.get().formatGender(result.first.appearance.gender)

            b.childHeight.text = textFormatter.get().formatHeight(result.first.appearance.heightRange)

            b.childSkin.text = textFormatter.get().formatSkin(result.first.appearance.skin)

            b.childHair.text = textFormatter.get().formatHair(result.first.appearance.hair)

            b.childLocation.text = textFormatter.get().formatLocation(result.first.location)

            b.notes.text = textFormatter.get().formatNotes(result.first.notes)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
