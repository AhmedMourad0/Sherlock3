package inc.ahmedmourad.sherlock.view.fragments.children

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import arrow.core.orNull
import com.bumptech.glide.Glide
import com.jaygoo.widget.RangeSeekBar
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.bundlizer.bundle
import inc.ahmedmourad.sherlock.bundlizer.unbundle
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.AddChildViewModelQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentAddChildBinding
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.domain.utils.exhaust
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.utils.defaults.DefaultOnRangeChangedListener
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.colors.ColorSelector
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.utils.pickers.places.PlacePicker
import inc.ahmedmourad.sherlock.viewmodel.common.GlobalViewModel
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.AddChildViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import kotlin.math.roundToInt

//TODO: maybe never allow publishing until all publishing operations and finished?
internal class AddChildFragment : Fragment(R.layout.fragment_add_child), View.OnClickListener {

    @Inject
    @field:AddChildViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    internal lateinit var placePicker: Lazy<PlacePicker>

    @Inject
    internal lateinit var imagePicker: Lazy<ImagePicker>

    private lateinit var skinColorSelector: ColorSelector<Skin>
    private lateinit var hairColorSelector: ColorSelector<Hair>

    private val globalViewModel: GlobalViewModel by activityViewModels()
    private val viewModel: AddChildViewModel by viewModels { viewModelFactory }

    private val args: AddChildFragmentArgs by navArgs()
    private var binding: FragmentAddChildBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusAddChildFragmentComponent().inject(this)

        globalViewModel.internetConnectivity.observe(viewLifecycleOwner, Observer { either ->
            when (either) {
                is Either.Left -> {
                    Timber.error(either.a, either.a::toString)
                    setInternetDependantViewsEnabled(false)
                }
                is Either.Right -> {
                    handlePublishingStateValue(viewModel.publishingState.value?.orNull())
                }
            }.exhaust()
        })

        viewModel.publishingState.observe(viewLifecycleOwner, Observer { either ->
            when (either) {
                is Either.Left -> {
                    Timber.error(either.a, either.a::toString)
                }
                is Either.Right -> {
                    handlePublishingStateValue(either.b)
                }
            }.exhaust()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddChildBinding.bind(view)

        val navigationChild = args.child?.unbundle(AppPublishedChild.serializer())

        if (navigationChild != null) {
            handleExternalNavigation(navigationChild)
        } else {
            setUserInteractionsEnabled(true)
        }

        arrayOf(::createSkinColorViews,
                ::createHairColorViews,
                ::initializeSeekBars,
                ::initializeEditTexts,
                ::initializeGenderRadioGroup,
                ::initializePictureImageView,
                ::initializeLocationTextView).forEach { it() }

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

    private fun publish() {
        setUserInteractionsEnabled(false)
        viewModel.onPublish()
    }

    private fun handleExternalNavigation(navigationChild: AppPublishedChild) {
        viewModel.take(navigationChild)
    }

    private fun handlePublishingStateValue(value: PublishingState?) {
        when (value) {
            is PublishingState.Success -> moveToChildDetailsFragment(value.child)
            is PublishingState.Ongoing -> setUserInteractionsEnabled(false)
            is PublishingState.Failure, null -> {
                setUserInteractionsEnabled(true)
                setInternetDependantViewsEnabled(globalViewModel.internetConnectivity.value?.orNull()
                        ?: false)
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

    private fun createSkinColorViews() {
        binding?.let { b ->
            skinColorSelector = ColorSelector(
                    ColorSelector.newItem(Skin.WHITE, b.skin.skinWhite, R.color.colorSkinWhite),
                    ColorSelector.newItem(Skin.WHEAT, b.skin.skinWheat, R.color.colorSkinWheat),
                    ColorSelector.newItem(Skin.DARK, b.skin.skinDark, R.color.colorSkinDark),
                    default = viewModel.skin.value ?: Skin.WHITE
            ).apply {
                onSelectionChangeListeners.add { viewModel.skin.value = it }
            }
        }
    }

    private fun createHairColorViews() {
        binding?.let { b ->
            hairColorSelector = ColorSelector(
                    ColorSelector.newItem(Hair.BLONDE, b.hair.hairBlonde, R.color.colorHairBlonde),
                    ColorSelector.newItem(Hair.BROWN, b.hair.hairBrown, R.color.colorHairBrown),
                    ColorSelector.newItem(Hair.DARK, b.hair.hairDark, R.color.colorHairDark),
                    default = viewModel.hair.value ?: Hair.BLONDE
            ).apply {
                onSelectionChangeListeners.add { viewModel.hair.value = it }
            }
        }
    }

    //TODO: consider removing all listeners and using observe and onSaveInstanceState instead
    private fun initializeEditTexts() {
        binding?.let { b ->
            b.name.firstNameEditText.setText(viewModel.firstName.value)
            b.name.lastNameEditText.setText(viewModel.lastName.value)
            b.notesEditText.setText(viewModel.notes.value)

            b.name.firstNameEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.firstName.value = s.toString()
                }
            })

            b.name.lastNameEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.lastName.value = s.toString()
                }
            })

            b.notesEditText.addTextChangedListener(object : DefaultTextWatcher {
                override fun afterTextChanged(s: Editable) {
                    viewModel.notes.value = s.toString()
                }
            })
        }
    }

    private fun initializeGenderRadioGroup() {
        binding?.let { b ->

            b.gender.genderRadioGroup.check(if (viewModel.gender.value == Gender.MALE)
                R.id.male_radio_button
            else
                R.id.female_radio_button
            )

            b.gender.genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.gender.value = if (checkedId == R.id.male_radio_button)
                    Gender.MALE
                else
                    Gender.FEMALE
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
                    viewModel.minAge.value = min.roundToInt()
                    viewModel.maxAge.value = max.roundToInt()
                }
            })

            b.heightSeekBar.setIndicatorTextDecimalFormat("###")
            b.heightSeekBar.setValue(
                    viewModel.minHeight.value?.toFloat() ?: 50f,
                    viewModel.maxHeight.value?.toFloat() ?: 70f
            )
            b.heightSeekBar.setOnRangeChangedListener(object : DefaultOnRangeChangedListener {
                override fun onRangeChanged(view: RangeSeekBar?, min: Float, max: Float, isFromUser: Boolean) {
                    viewModel.minHeight.value = min.roundToInt()
                    viewModel.maxHeight.value = max.roundToInt()
                }
            })
        }
    }

    private fun initializePictureImageView() {
        viewModel.picturePath.observe(viewLifecycleOwner, Observer {
            binding?.let {
                Glide.with(appCtx)
                        .load(it)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(it.pictureImageView)
            }
        })
    }

    private fun initializeLocationTextView() {
        viewModel.location.observe(viewLifecycleOwner, Observer { location: Location? ->
            if (location?.name?.isNotBlank() == true) {
                binding?.locationTextView?.text = location.name
            } else {
                binding?.locationTextView?.setText(R.string.no_location_specified)
            }
        })
    }

    private fun startImagePicker() {
        setPictureEnabled(false)
        imagePicker.get().start(requireActivity()) {
            setPictureEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    //TODO: should only start when connected to the internet, not the only one though
    private fun startPlacePicker() {
        setLocationEnabled(false)
        placePicker.get().start(requireActivity()) {
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

        placePicker.get().handleActivityResult(requestCode, data) { locationEither ->
            locationEither.fold(ifLeft = {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }, ifRight = viewModel.location::setValue)
        }

        imagePicker.get().handleActivityResult(requestCode, data, viewModel.picturePath::setValue)

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
