package inc.ahmedmourad.sherlock.view.controllers.children

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.jaygoo.widget.RangeSeekBar
import dagger.Lazy
import de.hdodenhof.circleimageview.CircleImageView
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildDetailsControllerFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.MainActivityIntentFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.AddChildViewModelQualifier
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.model.common.DeeplyLinkedController
import inc.ahmedmourad.sherlock.model.common.ParcelableWrapper
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.model.common.parcelize
import inc.ahmedmourad.sherlock.utils.defaults.DefaultOnRangeChangedListener
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.colors.ColorSelector
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.utils.pickers.places.PlacePicker
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.AddChildViewModel
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import kotlin.math.roundToInt

//TODO: maybe never allow publishing until all publishing operations and finished?
internal class AddChildController(args: Bundle) : LifecycleController(args), View.OnClickListener {

    @BindView(R.id.skin_white)
    internal lateinit var skinWhiteView: View

    @BindView(R.id.skin_wheat)
    internal lateinit var skinWheatView: View

    @BindView(R.id.skin_dark)
    internal lateinit var skinDarkView: View

    @BindView(R.id.hair_blonde)
    internal lateinit var hairBlondView: View

    @BindView(R.id.hair_brown)
    internal lateinit var hairBrownView: View

    @BindView(R.id.hair_dark)
    internal lateinit var hairDarkView: View

    @BindView(R.id.first_name_edit_text)
    internal lateinit var firstNameEditText: TextInputEditText

    @BindView(R.id.last_name_edit_text)
    internal lateinit var lastNameEditText: TextInputEditText

    @BindView(R.id.gender_radio_group)
    internal lateinit var genderRadioGroup: RadioGroup

    @BindView(R.id.add_child_age_seek_bar)
    internal lateinit var ageSeekBar: RangeSeekBar

    @BindView(R.id.add_child_height_seek_bar)
    internal lateinit var heightSeekBar: RangeSeekBar

    @BindView(R.id.add_child_location_text_view)
    internal lateinit var locationTextView: MaterialTextView

    @BindView(R.id.add_child_location_image_view)
    internal lateinit var locationImageView: ImageView

    @BindView(R.id.add_child_picture_image_view)
    internal lateinit var pictureImageView: CircleImageView

    @BindView(R.id.add_child_picture_text_view)
    internal lateinit var pictureTextView: MaterialTextView

    @BindView(R.id.add_child_notes_edit_text)
    internal lateinit var notesEditText: TextInputEditText

    @BindView(R.id.add_child_publish_button)
    internal lateinit var publishButton: MaterialButton

    @Inject
    @field:AddChildViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    internal lateinit var childDetailsControllerFactory: ChildDetailsControllerFactory

    @Inject
    internal lateinit var placePicker: Lazy<PlacePicker>

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    private lateinit var skinColorSelector: ColorSelector<Skin>
    private lateinit var hairColorSelector: ColorSelector<Hair>

    private lateinit var viewModel: AddChildViewModel

    private lateinit var context: Context

    private lateinit var unbinder: Unbinder

    private var publishingDisposable by disposable()
    private var internetConnectionDisposable by disposable()
    private var internetConnectivitySingleDisposable by disposable()

    constructor() : this(Bundle(0))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.addChildComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_add_child, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(viewModelFactory)[AddChildViewModel::class.java]

        val navigationChild = args.getParcelable<ParcelableWrapper<AppPublishedChild>>(ARG_CHILD)?.value

        if (navigationChild != null) {
            handleExternalNavigation(navigationChild)
        } else {
            setEnabledAndIdle(true)
        }

        arrayOf(::createSkinColorViews,
                ::createHairColorViews,
                ::initializeSeekBars,
                ::initializeEditTexts,
                ::initializeGenderRadioGroup,
                ::initializePictureImageView,
                ::initializeLocationTextView).forEach { it() }

        arrayOf(locationImageView,
                locationTextView,
                pictureImageView,
                publishButton,
                skinWhiteView,
                skinWheatView,
                skinDarkView,
                hairBlondView,
                hairBrownView,
                hairDarkView,
                pictureTextView,
                pictureTextView
        ).forEach { it.setOnClickListener(this) }

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        // we only handle connection (enabling and disabling internet-dependant
        // views) if publishing isn't underway
        internetConnectionDisposable = viewModel.internetConnectivityFlowable
                .subscribe({ (isConnected, publishingState) ->
                    if (publishingState == null) {
                        setEnabledAndIdle(true)
                        handleConnectionStateChange(isConnected)
                    } else {
                        handlePublishingStateValue(publishingState)
                    }
                }, {
                    Timber.error(it, it::toString)
                })
    }

    private fun publish() {

        publishingDisposable = viewModel.publishingStateFlowable
                .skip(1)
                .subscribe(this::handlePublishingStateValue) {
                    Timber.error(it, it::toString)
                }

        setEnabledAndIdle(false)
        viewModel.onPublish()
    }

    private fun handleExternalNavigation(navigationChild: AppPublishedChild) {

        viewModel.take(navigationChild)

        publishingDisposable = viewModel.publishingStateFlowable
                .subscribe(this::handlePublishingStateValue) {
                    Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                    Timber.error(it, it::toString)
                }
    }

    private fun handlePublishingStateValue(value: PublishingState) {
        when (value) {
            is PublishingState.Success -> onPublishedSuccessfully(value.child)
            is PublishingState.Failure -> onPublishingFailed()
            is PublishingState.Ongoing -> onPublishingOngoing()
        }
    }

    private fun onPublishedSuccessfully(child: RetrievedChild) {
        publishingDisposable?.dispose()
        when (val simpleChild = child.simplify()) {

            is Either.Left -> {
                Timber.error(ModelConversionException(simpleChild.a.toString()), simpleChild.a::toString)
                router.popCurrentController()
            }

            is Either.Right -> {
                val taggedController = childDetailsControllerFactory(simpleChild.b)
                router.popCurrentController()
                router.pushController(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
            }
        }
    }

    private fun onPublishingFailed() {
        publishingDisposable?.dispose()
        setEnabledAndIdle(true)
    }

    private fun onPublishingOngoing() {
        setEnabledAndIdle(false)
    }

    private fun setEnabledAndIdle(enabled: Boolean) {

        //TODO: start loading when false and stop when true
        arrayOf(skinWhiteView,
                skinWheatView,
                skinDarkView,
                hairBlondView,
                hairBrownView,
                hairDarkView,
                firstNameEditText,
                lastNameEditText,
                genderRadioGroup,
                ageSeekBar,
                heightSeekBar,
                locationTextView,
                locationImageView,
                pictureImageView,
                pictureTextView,
                notesEditText,
                publishButton
        ).forEach { it.isEnabled = enabled }

        // we disable internet-dependant views if no longer publishing and there's no internet connection
        internetConnectivitySingleDisposable = viewModel.internetConnectivitySingle
                .map { enabled && !it }
                .filter { it }
                .subscribe({
                    handleConnectionStateChange(false)
                }, {
                    Timber.error(it, it::toString)
                })
    }

    private fun handleConnectionStateChange(connected: Boolean) {
        setLocationEnabled(connected)
        publishButton.isEnabled = connected
    }

    private fun createSkinColorViews() {
        skinColorSelector = ColorSelector(
                ColorSelector.newItem(Skin.WHITE, skinWhiteView, R.color.colorSkinWhite),
                ColorSelector.newItem(Skin.WHEAT, skinWheatView, R.color.colorSkinWheat),
                ColorSelector.newItem(Skin.DARK, skinDarkView, R.color.colorSkinDark),
                default = viewModel.skin.value ?: Skin.WHITE
        ).apply {
            onSelectionChangeListeners.add { viewModel.skin.value = it }
        }
    }

    private fun createHairColorViews() {
        hairColorSelector = ColorSelector(
                ColorSelector.newItem(Hair.BLONDE, hairBlondView, R.color.colorHairBlonde),
                ColorSelector.newItem(Hair.BROWN, hairBrownView, R.color.colorHairBrown),
                ColorSelector.newItem(Hair.DARK, hairDarkView, R.color.colorHairDark),
                default = viewModel.hair.value ?: Hair.BLONDE
        ).apply {
            onSelectionChangeListeners.add { viewModel.hair.value = it }
        }
    }

    //TODO: consider removing all listeners and using observe and onSaveInstanceState instead
    private fun initializeEditTexts() {

        firstNameEditText.setText(viewModel.firstName.value)
        lastNameEditText.setText(viewModel.lastName.value)
        notesEditText.setText(viewModel.notes.value)

        firstNameEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.firstName.value = s.toString()
            }
        })

        lastNameEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.lastName.value = s.toString()
            }
        })

        notesEditText.addTextChangedListener(object : DefaultTextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.notes.value = s.toString()
            }
        })
    }

    private fun initializeGenderRadioGroup() {

        genderRadioGroup.check(if (viewModel.gender.value == Gender.MALE)
            R.id.male_radio_button
        else
            R.id.female_radio_button
        )

        genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.gender.value = if (checkedId == R.id.male_radio_button)
                Gender.MALE
            else
                Gender.FEMALE
        }
    }

    private fun initializeSeekBars() {

        ageSeekBar.setIndicatorTextDecimalFormat("##")
        ageSeekBar.setValue(
                viewModel.minAge.value?.toFloat() ?: 8f,
                viewModel.maxAge.value?.toFloat() ?: 12f
        )
        ageSeekBar.setOnRangeChangedListener(object : DefaultOnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                viewModel.minAge.value = min.roundToInt()
                viewModel.maxAge.value = max.roundToInt()
            }
        })

        heightSeekBar.setIndicatorTextDecimalFormat("###")
        heightSeekBar.setValue(
                viewModel.minHeight.value?.toFloat() ?: 50f,
                viewModel.maxHeight.value?.toFloat() ?: 70f
        )
        heightSeekBar.setOnRangeChangedListener(object : DefaultOnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, min: Float, max: Float, isFromUser: Boolean) {
                viewModel.minHeight.value = min.roundToInt()
                viewModel.maxHeight.value = max.roundToInt()
            }
        })
    }

    private fun initializePictureImageView() {
        viewModel.picturePath.observe(this, Observer {
            Glide.with(context)
                    .load(it)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(pictureImageView)
        })
    }

    private fun initializeLocationTextView() {
        viewModel.location.observe(this, Observer { location: Location? ->
            if (location?.name?.isNotBlank() == true) {
                locationTextView.text = location.name
            } else {
                locationTextView.setText(R.string.no_location_specified)
            }
        })
    }

    private fun startImagePicker() {
        setPictureEnabled(false)
        imagePicker.get().start(checkNotNull(activity)) {
            setPictureEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    //TODO: should only start when connected to the internet, not the only one though
    private fun startPlacePicker() {
        setLocationEnabled(false)
        placePicker.get().start(checkNotNull(activity)) {
            setLocationEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    private fun setPictureEnabled(enabled: Boolean) {
        pictureImageView.isEnabled = enabled
        pictureTextView.isEnabled = enabled
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        setLocationEnabled(true)
        setPictureEnabled(true)

        if (resultCode != RESULT_OK)
            return

        checkNotNull(data) {
            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            "Parameter data is null!"
        }

        placePicker.get().handleActivityResult(requestCode, data) { locationEither ->
            locationEither.fold(ifLeft = {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }, ifRight = viewModel.location::setValue)
        }

        imagePicker.get().handleActivityResult(requestCode, data) { pictureEither ->
            pictureEither.fold(ifLeft = {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }, ifRight = viewModel.picturePath::setValue)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setLocationEnabled(enabled: Boolean) {
        locationImageView.isEnabled = enabled
        locationTextView.isEnabled = enabled
    }

    override fun onDetach(view: View) {
        publishingDisposable?.dispose()
        internetConnectionDisposable?.dispose()
        internetConnectivitySingleDisposable?.dispose()
        super.onDetach(view)
    }

    override fun onDestroy() {
        publishingDisposable?.dispose()
        internetConnectionDisposable?.dispose()
        internetConnectivitySingleDisposable?.dispose()
        unbinder.unbind()
        SherlockComponent.Controllers.addChildComponent.release()
        super.onDestroy()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.skin_white -> skinColorSelector.select(Skin.WHITE)

            R.id.skin_wheat -> skinColorSelector.select(Skin.WHEAT)

            R.id.skin_dark -> skinColorSelector.select(Skin.DARK)

            R.id.hair_blonde -> hairColorSelector.select(Hair.BLONDE)

            R.id.hair_brown -> hairColorSelector.select(Hair.BROWN)

            R.id.hair_dark -> hairColorSelector.select(Hair.DARK)

            R.id.add_child_location_image_view, R.id.add_child_location_text_view -> startPlacePicker()

            R.id.add_child_picture_image_view, R.id.add_child_picture_text_view -> startImagePicker()

            R.id.add_child_publish_button -> publish()
        }
    }

    companion object : DeeplyLinkedController {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.AddChildController"

        private const val ARG_CHILD = "inc.ahmedmourad.sherlock.view.controllers.arg.CHILD"

        private const val DESTINATION_ID = 4727

        private const val EXTRA_CHILD = "inc.ahmedmourad.sherlock.view.controllers.extra.CHILD"

        fun newInstance() = TaggedController(AddChildController(), CONTROLLER_TAG)

        private fun newInstance(child: AppPublishedChild) = TaggedController(AddChildController(Bundle(1).apply {
            putParcelable(ARG_CHILD, child.parcelize())
        }), CONTROLLER_TAG)

        fun createIntent(activityFactory: MainActivityIntentFactory, child: AppPublishedChild): Intent {
            return activityFactory(DESTINATION_ID).apply {
                putExtra(EXTRA_CHILD, child.parcelize())
            }
        }

        override fun isDestination(destinationId: Int): Boolean {
            return destinationId == DESTINATION_ID
        }

        override fun navigate(router: Router, intent: Intent) {

            val addChildControllerBackstackInstance = router.getControllerWithTag(CONTROLLER_TAG)

            if (addChildControllerBackstackInstance != null)
                router.popController(addChildControllerBackstackInstance)

            val taggedController = newInstance(
                    requireNotNull(
                            intent.getParcelableExtra<ParcelableWrapper<AppPublishedChild>>(EXTRA_CHILD)
                    ).value
            )

            router.pushController(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
        }
    }
}
