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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaygoo.widget.RangeSeekBar
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentAddChildBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.children.AppChildToPublish
import dev.ahmedmourad.sherlock.android.pickers.colors.ColorSelector
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.utils.DefaultOnRangeChangedListener
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.utils.somethingWentWrong
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.AddChildViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.constants.*
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import kotlinx.serialization.builtins.nullable
import splitties.init.appCtx
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
                AddChildViewModel.defaultArgs(args.child.unbundle(AppChildToPublish.serializer().nullable))
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
        (requireActivity() as BackdropActivity).setTitle(getString(R.string.found_a_child))

        setUserInteractionsEnabled(args.child.unbundle(AppChildToPublish.serializer().nullable) == null)

        initializeSkinColorViews()
        initializeHairColorViews()
        initializeSeekBars()
        initializeEditTexts()
        initializeGenderRadioGroup()
        initializePictureImageView()
        initializeLocationTextView()
        addErrorObservers()
        addOpacityObservers()

        observe(globalViewModel.internetConnectivityState, Observer { state ->
            when (state) {
                GlobalViewModel.InternetConnectivityState.Connected,
                GlobalViewModel.InternetConnectivityState.Disconnected -> {
                    handlePublishingStateValue(viewModel.publishingState.value)
                }
                GlobalViewModel.InternetConnectivityState.Loading -> Unit
                GlobalViewModel.InternetConnectivityState.Error -> {
                    setInternetDependantViewsEnabled(false)
                }
            }.exhaust()
        })

        observe(viewModel.publishingState, Observer {
            this.handlePublishingStateValue(it)
        })

        binding?.let { b ->
            arrayOf(b.locationImageView,
                    b.location,
                    b.childPicture,
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
            b.root.requestFocus()
        }
    }

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.userError,
                viewModel.firstNameError,
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
                viewModel.childError, observer = Observer { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onUserErrorHandled()
                viewModel.onFirstNameErrorHandled()
                viewModel.onLastNameErrorHandled()
                viewModel.onNameErrorHandled()
                viewModel.onMinAgeErrorHandled()
                viewModel.onMaxAgeErrorHandled()
                viewModel.onLocationErrorHandled()
                viewModel.onAgeErrorHandled()
                viewModel.onMinHeightErrorHandled()
                viewModel.onMaxHeightErrorHandled()
                viewModel.onHeightErrorHandled()
                viewModel.onAppearanceErrorHandled()
                viewModel.onPicturePathErrorHandled()
                viewModel.onChildErrorHandled()
                setUserInteractionsEnabled(true)
            }
        })
    }

    private fun addOpacityObservers() {
        binding?.let { b ->

            observe(viewModel.firstName, Observer {
                b.name.firstNameEditText.alpha = if (it == null) 0.3f else 0.6f
            })

            observe(viewModel.lastName, Observer {
                b.name.lastNameEditText.alpha = if (it == null) 0.3f else 0.6f
            })

            observe(viewModel.skin, Observer {
                b.skin.skinWhite.alpha = if (it == null) 0.6f else 1f
                b.skin.skinWheat.alpha = if (it == null) 0.6f else 1f
                b.skin.skinDark.alpha = if (it == null) 0.6f else 1f
                b.skin.skinTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.hair, Observer {
                b.hair.hairBlonde.alpha = if (it == null) 0.6f else 1f
                b.hair.hairBrown.alpha = if (it == null) 0.6f else 1f
                b.hair.hairDark.alpha = if (it == null) 0.6f else 1f
                b.hair.hairTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.gender, Observer {
                b.gender.genderRadioGroup.alpha = if (it == null) 0.6f else 1f
                b.gender.genderTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.location, Observer {
                b.location.alpha = if (it == null) 0.6f else 1f
                b.locationImageView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.minAge, Observer {
                b.ageSeekBar.alpha = if (it == null) 0.6f else 1f
                b.ageTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.maxAge, Observer {
                b.ageSeekBar.alpha = if (it == null) 0.6f else 1f
                b.ageTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.minHeight, Observer {
                b.heightSeekBar.alpha = if (it == null) 0.6f else 1f
                b.heightTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.maxHeight, Observer {
                b.heightSeekBar.alpha = if (it == null) 0.6f else 1f
                b.heightTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.notes, Observer {
                b.notesEditText.alpha = if (it == null) 0.3f else 0.6f
            })

            observe(viewModel.picturePath, Observer {
                b.pictureTextView.alpha = if (it == null) 0.6f else 1f
                b.childPicture.alpha = if (it == null) 0.6f else 1f
            })
        }
    }

    private fun publish() {
        setUserInteractionsEnabled(false)
        when (globalViewModel.userState.value) {

            is GlobalViewModel.UserState.Authenticated -> {
                viewModel.onPublish(globalViewModel.signedInUserSimplified)
            }

            is GlobalViewModel.UserState.Incomplete -> {
                Toast.makeText(appCtx, R.string.authentication_completion_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                setUserInteractionsEnabled(true)
            }

            GlobalViewModel.UserState.Unauthenticated -> {
                Toast.makeText(appCtx, R.string.authentication_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                setUserInteractionsEnabled(true)
            }

            GlobalViewModel.UserState.Loading -> {
                Toast.makeText(appCtx, R.string.authentication_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                setUserInteractionsEnabled(true)
            }

            GlobalViewModel.UserState.NoInternet -> {
                Toast.makeText(appCtx, R.string.internet_connection_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                setUserInteractionsEnabled(true)
            }

            GlobalViewModel.UserState.Error -> {
                Toast.makeText(appCtx, somethingWentWrong(), Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
                setUserInteractionsEnabled(true)
            }

            null -> {
                Toast.makeText(appCtx, somethingWentWrong(), Toast.LENGTH_LONG).show()
                setUserInteractionsEnabled(true)
            }
        }.exhaust()
    }

    private fun handlePublishingStateValue(value: PublishingState?) {
        when (value) {
            is PublishingState.Success -> {
                moveToChildDetailsFragment(value.child)
                value.consume()
            }
            is PublishingState.Ongoing -> setUserInteractionsEnabled(false)
            is PublishingState.Failure -> {
                setUserInteractionsEnabled(true)
                if (value.error is PublishingState.Exception.NoSignedInUserException) {
                    (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
                }
                setInternetDependantViewsEnabled(
                        globalViewModel.internetConnectivityState.value is GlobalViewModel.InternetConnectivityState.Connected
                )
                value.consume()
            }
            PublishingState.Idle, null -> {
                setUserInteractionsEnabled(true)
                setInternetDependantViewsEnabled(
                        globalViewModel.internetConnectivityState.value is GlobalViewModel.InternetConnectivityState.Connected
                )
            }
        }
    }

    private fun moveToChildDetailsFragment(child: RetrievedChild) {
        findNavController().apply {
            navigate(AddChildFragmentDirections.actionAddChildFragmentToChildDetailsFragment(
                    child.simplify().id.bundle(ChildId.serializer())
            ))
        }
    }

    private fun setUserInteractionsEnabled(enabled: Boolean) {
        binding?.let { b ->

            if (enabled) {
                b.contentRoot.visibility = View.VISIBLE
                b.loading.root.visibility = View.GONE
            } else {
                b.contentRoot.visibility = View.GONE
                b.loading.root.visibility = View.VISIBLE
            }

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
                    b.location,
                    b.locationImageView,
                    b.childPicture,
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
            ).apply {
                onSelectionChangeListeners.add { viewModel.onSkinChange(it?.value) }
            }
            b.skin.skinWhite.alpha = 0.6f
            b.skin.skinWheat.alpha = 0.6f
            b.skin.skinDark.alpha = 0.6f
        }
    }

    private fun initializeHairColorViews() {
        binding?.let { b ->
            hairColorSelector = ColorSelector(
                    ColorSelector.newItem(Hair.BLONDE, b.hair.hairBlonde, R.color.colorHairBlonde),
                    ColorSelector.newItem(Hair.BROWN, b.hair.hairBrown, R.color.colorHairBrown),
                    ColorSelector.newItem(Hair.DARK, b.hair.hairDark, R.color.colorHairDark),
                    default = viewModel.hair.value?.let { findEnum(it, Hair.values()) }
            ).apply {
                onSelectionChangeListeners.add { viewModel.onHairChange(it?.value) }
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
                    viewModel.maxHeight.value?.toFloat() ?: 80f
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
        observe(viewModel.picturePath, Observer { picturePath ->
            binding?.let { b ->
                imageLoader.get().load(
                        picturePath?.value,
                        b.childPicture,
                        R.drawable.placeholder,
                        R.drawable.placeholder
                )
            }
        })
    }

    private fun initializeLocationTextView() {
        observe(viewModel.location, Observer { location: PlacePicker.Location? ->
            if (location?.name?.isNotBlank() == true) {
                binding?.location?.text = location.name
            } else {
                binding?.location?.setText(R.string.no_location_specified)
            }
        })
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
            b.childPicture.isEnabled = enabled
            b.pictureTextView.isEnabled = enabled
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        setLocationEnabled(true)
        setPictureEnabled(true)

        if (resultCode != RESULT_OK) {
            return
        }

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
            b.location.isEnabled = enabled
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

            R.id.location_image_view, R.id.location -> startPlacePicker()

            R.id.child_picture, R.id.picture_text_view -> startImagePicker()

            R.id.publish_button -> publish()
        }
    }
}
