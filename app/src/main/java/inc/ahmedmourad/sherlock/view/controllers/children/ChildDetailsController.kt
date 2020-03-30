package inc.ahmedmourad.sherlock.view.controllers.children

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import arrow.core.Either
import arrow.core.Tuple2
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.MainActivityIntentFactory
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.DeeplyLinkedController
import inc.ahmedmourad.sherlock.model.common.ParcelableWrapper
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.model.common.parcelize
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.utils.setSupportActionBar
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.ChildDetailsViewModel
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.factories.ChildDetailsViewModelFactoryFactory
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class ChildDetailsController(args: Bundle) : LifecycleController(args) {

    @BindView(R.id.display_child_toolbar)
    internal lateinit var toolbar: MaterialToolbar

    @BindView(R.id.display_child_picture)
    internal lateinit var pictureImageView: ImageView

    @BindView(R.id.display_child_name)
    internal lateinit var nameTextView: MaterialTextView

    @BindView(R.id.display_child_age)
    internal lateinit var ageTextView: MaterialTextView

    @BindView(R.id.display_child_gender)
    internal lateinit var genderTextView: MaterialTextView

    @BindView(R.id.display_child_height)
    internal lateinit var heightTextView: MaterialTextView

    @BindView(R.id.display_child_skin)
    internal lateinit var skinTextView: MaterialTextView

    @BindView(R.id.display_child_hair)
    internal lateinit var hairTextView: MaterialTextView

    @BindView(R.id.display_child_location)
    internal lateinit var locationTextView: MaterialTextView

    @BindView(R.id.display_child_notes)
    internal lateinit var notesTextView: MaterialTextView

    @Inject
    internal lateinit var formatter: Lazy<Formatter>

    @Inject
    internal lateinit var viewModelFactoryFactory: ChildDetailsViewModelFactoryFactory

    private lateinit var viewModel: ChildDetailsViewModel

    private lateinit var context: Context

    private var findChildDisposable by disposable()

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.childDetailsComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_display_child, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        setSupportActionBar(toolbar)

        viewModel = viewModelProvider(
                viewModelFactoryFactory(
                        requireNotNull(
                                args.getParcelable<ParcelableWrapper<SimpleRetrievedChild>>(ARG_CHILD)
                        ).value
                )
        )[ChildDetailsViewModel::class.java]

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        //TODO: notify the user when the data is updated or deleted
        findChildDisposable = viewModel.result.subscribe({ resultEither ->
            when (resultEither) {

                is Either.Left -> {
                    Timber.error(resultEither.a, resultEither.a::toString)
                    Toast.makeText(context, resultEither.a.localizedMessage, Toast.LENGTH_LONG).show()
                    router.popCurrentController()
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

    override fun onDetach(view: View) {
        findChildDisposable?.dispose()
        super.onDetach(view)
    }

    private fun populateUi(result: Tuple2<RetrievedChild, Weight?>?) {

        if (result == null) {
            Toast.makeText(context, R.string.child_data_missing, Toast.LENGTH_LONG).show()
            router.popCurrentController()
            return
        }

        //TODO: we should inject glide
        Glide.with(appCtx)
                .load(result.a.pictureUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(pictureImageView)

        val name = formatter.get().formatName(result.a.name)
        toolbar.title = name
        nameTextView.text = name

        ageTextView.text = formatter.get().formatAge(result.a.appearance.ageRange)

        genderTextView.text = formatter.get().formatGender(result.a.appearance.gender)

        heightTextView.text = formatter.get().formatHeight(result.a.appearance.heightRange)

        skinTextView.text = formatter.get().formatSkin(result.a.appearance.skin)

        hairTextView.text = formatter.get().formatHair(result.a.appearance.hair)

        locationTextView.text = formatter.get().formatLocation(result.a.location)

        notesTextView.text = formatter.get().formatNotes(result.a.notes)
    }

    override fun onDestroy() {
        SherlockComponent.Controllers.childDetailsComponent.release()
        findChildDisposable?.dispose()
        unbinder.unbind()
        super.onDestroy()
    }

    companion object : DeeplyLinkedController {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.DisplayChildController"

        private const val ARG_CHILD = "inc.ahmedmourad.sherlock.view.controllers.arg.CHILD"

        private const val DESTINATION_ID = 3763

        private const val EXTRA_CHILD = "inc.ahmedmourad.sherlock.view.controllers.extra.CHILD"

        fun newInstance(child: SimpleRetrievedChild) = TaggedController(ChildDetailsController(Bundle(1).apply {
            putParcelable(ARG_CHILD, child.parcelize())
        }), CONTROLLER_TAG)

        fun createIntent(activityFactory: MainActivityIntentFactory, child: SimpleRetrievedChild): Intent {
            return activityFactory(DESTINATION_ID).apply {
                putExtra(EXTRA_CHILD, child.parcelize())
            }
        }

        override fun isDestination(destinationId: Int): Boolean {
            return destinationId == DESTINATION_ID
        }

        override fun navigate(router: Router, intent: Intent) {

            val taggedController = newInstance(
                    requireNotNull(
                            intent.getParcelableExtra<ParcelableWrapper<SimpleRetrievedChild>>(EXTRA_CHILD)
                    ).value
            )

            router.pushController(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
        }
    }
}
