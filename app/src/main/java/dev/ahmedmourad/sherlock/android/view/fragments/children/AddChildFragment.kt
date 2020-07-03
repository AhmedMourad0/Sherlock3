package dev.ahmedmourad.sherlock.android.view.fragments.children

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import arrow.core.orNull
import com.jaygoo.widget.RangeSeekBar
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentAddChildBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.children.AppPublishedChild
import dev.ahmedmourad.sherlock.android.pickers.colors.ColorSelector
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.utils.DefaultOnRangeChangedListener
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.AddChildViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.constants.*
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.serialization.builtins.nullable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

//TODO: maybe never allow publishing until all publishing operations and finished?
internal class AddChildFragment : Fragment(R.layout.fragment_add_child), View.OnClickListener {

    @Inject
    internal lateinit var placePicker: Lazy<PlacePicker>

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>


    @Inject
    internal lateinit var imageLoader: Lazy<ImageLoader>

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<AddChildViewModel>>

    private val globalViewModel: GlobalViewModel by activityViewModels()
    private val viewModel: AddChildViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                AddChildViewModel.defaultArgs(args.child.unbundle(AppPublishedChild.serializer().nullable))
        )
    }

    private lateinit var skinColorSelector: ColorSelector<Skin>
    private lateinit var hairColorSelector: ColorSelector<Hair>

    private val args: AddChildFragmentArgs by navArgs()
    private var binding: FragmentAddChildBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddChildBinding.bind(view)

        setUserInteractionsEnabled(args.child.unbundle(AppPublishedChild.serializer().nullable) == null)

        initializeSkinColorViews()
        initializeHairColorViews()
        initializeSeekBars()
        initializeEditTexts()
        initializeGenderRadioGroup()
        initializePictureImageView()
        initializeLocationTextView()
        addErrorObservers()

        observe(globalViewModel.internetConnectivity) { either: Either<ObserveInternetConnectivityInteractor.Exception, Boolean> ->
            when (either) {
                is Either.Left -> {
                    Timber.error(RuntimeException(either.a.toString()), either.a::toString)
                    setInternetDependantViewsEnabled(false)
                }
                is Either.Right -> {
                    handlePublishingStateValue(viewModel.publishingState.value)
                }
            }.exhaust()
        }

        observe(viewModel.publishingState, this::handlePublishingStateValue)

        binding?.let { b ->
            arrayOf(b.locationImageView,
                    b.locationTextView,
                    b.pictureImageView,
                    b.publishButton,
                    b.skin.skinWhite,
                    b.skin.skinWheat,
                    b.skin.skinDark,
                    b.hair.hairBlonde,
                    b.hair.hairBrown,
                    b.hair.hairDark,
                    b.pictureTextView,
                    b.pictureTextView
            ).forEach { it.setOnClickListener(this) }
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.firstNameError,
                viewModel.lastNameError,
                viewModel.nameError,
                viewModel.minAgeError,
                viewModel.maxAgeError,
                viewModel.locationError,
                viewModel.ageError,
                viewModel.minHeightError,
                viewModel.maxHeightError,
                viewModel.heightError,
                viewModel.appearanceError,
                viewModel.picturePathError,
                viewModel.childError
        ) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onFirstNameErrorDismissed()
                viewModel.onLastNameErrorDismissed()
                viewModel.onNameErrorDismissed()
                viewModel.onMinAgeErrorDismissed()
                viewModel.onMaxAgeErrorDismissed()
                viewModel.onLocationErrorDismissed()
                viewModel.onAgeErrorDismissed()
                viewModel.onMinHeightErrorDismissed()
                viewModel.onMaxHeightErrorDismissed()
                viewModel.onHeightErrorDismissed()
                viewModel.onAppearanceErrorDismissed()
                viewModel.onPicturePathErrorDismissed()
                viewModel.onChildErrorDismissed()
                setUserInteractionsEnabled(true)
            }
        }
    }

    private fun publish() {
        setUserInteractionsEnabled(false)
        viewModel.onPublish()
    }

    private fun handlePublishingStateValue(value: PublishingState?) {
        when (value) {
            is PublishingState.Success -> moveToChildDetailsFragment(value.child)
            is PublishingState.Ongoing -> setUserInteractionsEnabled(false)
            is PublishingState.Failure -> {
                setUserInteractionsEnabled(true)
                if (value.error is PublishingState.Exception.NoSignedInUserException) {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                } else {
                    setInternetDependantViewsEnabled(
                            globalViewModel.internetConnectivity.value?.orNull() ?: false
                    )
                }
            }
            null -> {
                setUserInteractionsEnabled(true)
                setInternetDependantViewsEnabled(
                        globalViewModel.internetConnectivity.value?.orNull() ?: false
                )
            }
        }
    }

    private fun moveToChildDetailsFragment(child: RetrievedChild) {
        findNavController().apply {
            popBackStack()
            navigate(AddChildFragmentDirections.actionAddChildFragmentToChildDetailsFragment(
                    child.simplify().bundle(SimpleRetrievedChild.serializer())
            ))
        }
    }

    private fun setUserInteractionsEnabled(enabled: Boolean) {

        //TODO: start loading when false and stop when true
        binding?.let { b ->
            arrayOf(b.skin.skinWhite,
                    b.skin.skinWheat,
                    b.skin.skinDark,
                    b.hair.hairBlonde,
                    b.hair.hairBrown,
                    b.hair.hairDark,
                    b.name.firstNameEditText,
                    b.name.lastNameEditText,
                    b.gender.genderRadioGroup,
                    b.ageSeekBar,
                    b.heightSeekBar,
                    b.locationTextView,
                    b.locationImageView,
                    b.pictureImageView,
                    b.pictureTextView,
                    b.notesEditText,
                    b.publishButton
            ).forEach { it.isEnabled = enabled }
        }
    }

    private fun setInternetDependantViewsEnabled(enabled: Boolean) {
        setLocationEnabled(enabled)
        binding?.publishButton?.isEnabled = enabled
    }

    private fun initializeSkinColorViews() {
        binding?.let { b ->
            skinColorSelector = ColorSelector(
                    ColorSelector.newItem(Skin.WHITE, b.skin.skinWhite, R.color.colorSkinWhite),
                    ColorSelector.newItem(Skin.WHEAT, b.skin.skinWheat, R.color.colorSkinWheat),
                    ColorSelector.newItem(Skin.DARK, b.skin.skinDark, R.color.colorSkinDark),
                    default = viewModel.skin.value?.let { findEnum(it, Skin.values()) }
                            ?: Skin.WHITE
            ).apply {
                onSelectionChangeListeners.add { viewModel.onSkinChange(it.value) }
            }
        }
    }

    private fun initializeHairColorViews() {
        binding?.let { b ->
            hairColorSelector = ColorSelector(
                    ColorSelector.newItem(Hair.BLONDE, b.hair.hairBlonde, R.color.colorHairBlonde),
                    ColorSelector.newItem(Hair.BROWN, b.hair.hairBrown, R.color.colorHairBrown),
                    ColorSelector.newItem(Hair.DARK, b.hair.hairDark, R.color.colorHairDark),
                    default = viewModel.hair.value?.let { findEnum(it, Hair.values()) }
                            ?: Hair.BLONDE
            ).apply {
                onSelectionChangeListeners.add { viewModel.onHairChange(it.value) }
            }
        }
    }

    private fun initializeEditTexts() {
        binding?.let { b ->

            b.name.firstNameEditText.setText(viewModel.firstName.value)
            b.name.lastNameEditText.setText(viewModel.lastName.value)
            b.notesEditText.setText(viewModel.notes.value)

            b.name.firstNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onFirstNameChange(text.toString())
            }

            b.name.lastNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onLastNameChange(text.toString())
            }

            b.notesEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onNotesChange(text.toString())
            }
        }
    }

    private fun initializeGenderRadioGroup() {
        binding?.let { b ->

            when (viewModel.gender.value) {
                Gender.MALE.value -> b.gender.genderRadioGroup.check(R.id.male_radio_button)
                Gender.FEMALE.value -> b.gender.genderRadioGroup.check(R.id.female_radio_button)
                null -> b.gender.genderRadioGroup.clearCheck()
            }

            b.gender.genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.onGenderChange(if (checkedId == R.id.male_radio_button) {
                    Gender.MALE.value
                } else {
                    Gender.FEMALE.value
                })
            }
        }
    }

    private fun initializeSeekBars() {
        binding?.let { b ->

            b.ageSeekBar.setIndicatorTextDecimalFormat("##")
            b.ageSeekBar.setValue(
                    viewModel.minAge.value?.toFloat() ?: 8f,
                    viewModel.maxAge.value?.toFloat() ?: 12f
            )
            b.ageSeekBar.setOnRangeChangedListener(object : DefaultOnRangeChangedListener {
                override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                    viewModel.onMinAgeChange(min.roundToInt())
                    viewModel.onMaxAgeChange(max.roundToInt())
                }
            })

            b.heightSeekBar.setIndicatorTextDecimalFormat("###")
            b.heightSeekBar.setValue(
                    viewModel.minHeight.value?.toFloat() ?: 50f,
                    viewModel.maxHeight.value?.toFloat() ?: 70f
            )
            b.heightSeekBar.setOnRangeChangedListener(object : DefaultOnRangeChangedListener {
                override fun onRangeChanged(view: RangeSeekBar?, min: Float, max: Float, isFromUser: Boolean) {
                    viewModel.onMinHeightChange(min.roundToInt())
                    viewModel.onMaxHeightChange(max.roundToInt())
                }
            })
        }
    }

    private fun initializePictureImageView() {
        observe(viewModel.picturePath) { picturePath ->
            binding?.let { b ->
                imageLoader.get().load(
                        picturePath?.value,
                        b.pictureImageView,
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
            }
        }
    }

    private fun initializeLocationTextView() {
        observe(viewModel.location) { location: PlacePicker.Location? ->
            if (location?.name?.isNotBlank() == true) {
                binding?.locationTextView?.text = location.name
            } else {
                binding?.locationTextView?.setText(R.string.no_location_specified)
            }
        }
    }

    private fun startImagePicker() {
        setPictureEnabled(false)
        imagePicker.get().start(this) {
            setPictureEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    //TODO: should only start when connected to the internet, not the only one though
    private fun startPlacePicker() {
        setLocationEnabled(false)
        placePicker.get().start(this) {
            setLocationEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    private fun setPictureEnabled(enabled: Boolean) {
        binding?.let { b ->
            b.pictureImageView.isEnabled = enabled
            b.pictureTextView.isEnabled = enabled
        }
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

        placePicker.get().handleActivityResult(requestCode, data, viewModel::onLocationChange)
        imagePicker.get().handleActivityResult(requestCode, data, viewModel::onPicturePathChange)

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setLocationEnabled(enabled: Boolean) {
        binding?.let { b ->
            b.locationImageView.isEnabled = enabled
            b.locationTextView.isEnabled = enabled
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.skin_white -> skinColorSelector.select(Skin.WHITE)

            R.id.skin_wheat -> skinColorSelector.select(Skin.WHEAT)

            R.id.skin_dark -> skinColorSelector.select(Skin.DARK)

            R.id.hair_blonde -> hairColorSelector.select(Hair.BLONDE)

            R.id.hair_brown -> hairColorSelector.select(Hair.BROWN)

            R.id.hair_dark -> hairColorSelector.select(Hair.DARK)

            R.id.location_image_view, R.id.location_text_view -> startPlacePicker()

            R.id.picture_image_view, R.id.picture_text_view -> startImagePicker()

            R.id.publish_button -> publish()
        }
    }
}
