package inc.ahmedmourad.sherlock.view.fragments.children

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import arrow.core.Tuple2
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.databinding.FragmentChildDetailsBinding
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.ChildDetailsViewModel
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories.ChildDetailsViewModelFactoryFactory
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ChildDetailsFragment : Fragment(R.layout.fragment_child_details) {

    @Inject
    internal lateinit var formatter: Lazy<Formatter>

    @Inject
    internal lateinit var viewModelFactoryFactory: ChildDetailsViewModelFactoryFactory

    private lateinit var viewModel: ChildDetailsViewModel

    private var findChildDisposable by disposable()

    private val args: ChildDetailsFragmentArgs by navArgs()
    private var binding: FragmentChildDetailsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusChildDetailsFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChildDetailsBinding.bind(view)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding?.toolbar)

        viewModel = ViewModelProvider(this,
                viewModelFactoryFactory(args.simpleChild.unbundle(SimpleRetrievedChild.serializer()))
        )[ChildDetailsViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        //TODO: notify the user when the data is updated or deleted
        findChildDisposable = viewModel.result.subscribe({ resultEither ->
            when (resultEither) {

                is Either.Left -> {
                    Timber.error(resultEither.a, resultEither.a::toString)
                    Toast.makeText(context, resultEither.a.localizedMessage, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }

                is Either.Right -> {
                    populateUi(resultEither.b)
                }
            }
        }, {
            Timber.error(it, it::toString)
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
        })
    }

    private fun populateUi(result: Tuple2<RetrievedChild, Weight?>?) {

        if (result == null) {
            //TODO: display error message and image instead
            Toast.makeText(context, R.string.child_data_missing, Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        binding?.let {

            //TODO: we should inject glide
            Glide.with(appCtx)
                    .load(result.a.pictureUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(it.childPicture)


            val name = formatter.get().formatName(result.a.name)
            it.toolbar.title = name
            it.childName.text = name

            it.childAge.text = formatter.get().formatAge(result.a.appearance.ageRange)

            it.childGender.text = formatter.get().formatGender(result.a.appearance.gender)

            it.childHeight.text = formatter.get().formatHeight(result.a.appearance.heightRange)

            it.childSkin.text = formatter.get().formatSkin(result.a.appearance.skin)

            it.childHair.text = formatter.get().formatHair(result.a.appearance.hair)

            it.childLocation.text = formatter.get().formatLocation(result.a.location)

            it.notes.text = formatter.get().formatNotes(result.a.notes)
        }
    }

    override fun onStop() {
        findChildDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
