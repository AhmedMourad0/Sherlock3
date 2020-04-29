package dev.ahmedmourad.sherlock.android.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import arrow.core.Tuple2
import com.bumptech.glide.Glide
import dagger.Lazy
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.databinding.FragmentChildDetailsBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ChildDetailsViewModel
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class ChildDetailsFragment : Fragment(R.layout.fragment_child_details) {

    @Inject
    internal lateinit var textFormatter: Lazy<TextFormatter>

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

        //TODO: notify the user when the data is updated or deleted
        observe(viewModel.result) { resultEither ->
            when (resultEither) {
                is Either.Left -> {
                    Toast.makeText(context, resultEither.a.localizedMessage, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    Timber.error(resultEither.a, resultEither.a::toString)
                }
                is Either.Right -> {
                    populateUi(resultEither.b)
                }
            }.exhaust()
        }
    }

    private fun populateUi(result: Tuple2<RetrievedChild, Weight?>?) {

        if (result == null) {
            //TODO: display error message and image instead
            Toast.makeText(context, R.string.child_data_missing, Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        binding?.let { b ->

            //TODO: we should inject glide
            Glide.with(appCtx)
                    .load(result.a.pictureUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(b.childPicture)


            val name = textFormatter.get().formatName(result.a.name)
            b.toolbar.title = name
            b.childName.text = name

            b.childAge.text = textFormatter.get().formatAge(result.a.appearance.ageRange)

            b.childGender.text = textFormatter.get().formatGender(result.a.appearance.gender)

            b.childHeight.text = textFormatter.get().formatHeight(result.a.appearance.heightRange)

            b.childSkin.text = textFormatter.get().formatSkin(result.a.appearance.skin)

            b.childHair.text = textFormatter.get().formatHair(result.a.appearance.hair)

            b.childLocation.text = textFormatter.get().formatLocation(result.a.location)

            b.notes.text = textFormatter.get().formatNotes(result.a.notes)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
