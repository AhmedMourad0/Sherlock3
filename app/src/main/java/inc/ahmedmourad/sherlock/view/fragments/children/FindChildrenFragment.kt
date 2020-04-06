package inc.ahmedmourad.sherlock.view.fragments.children

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.findAppComponent
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.FindChildrenViewModelQualifier
import inc.ahmedmourad.sherlock.databinding.FragmentFindChildrenBinding
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.colors.ColorSelector
import inc.ahmedmourad.sherlock.utils.pickers.places.PlacePicker
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.FindChildrenViewModel
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class FindChildrenFragment : Fragment(R.layout.fragment_find_children), View.OnClickListener {

    @Inject
    @field:FindChildrenViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    internal lateinit var placePicker: Lazy<PlacePicker>

    private lateinit var skinColorSelector: ColorSelector<Skin>
    private lateinit var hairColorSelector: ColorSelector<Hair>

    private lateinit var viewModel: FindChildrenViewModel

    private var internetConnectionDisposable by disposable()

    private var binding: FragmentFindChildrenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appCtx.findAppComponent().plusFindChildrenFragmentComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFindChildrenBinding.bind(view)
        viewModel = ViewModelProvider(this, viewModelFactory)[FindChildrenViewModel::class.java]

        arrayOf(::createSkinColorViews,
                ::createHairColorViews,
                ::initializeEditTexts,
                ::initializeGenderRadioGroup,
                ::initializeLocationTextView,
                ::initializeNumberPickers).forEach { it() }

        binding?.let { b ->
            arrayOf(b.locationImageView,
                    b.locationTextView,
                    b.skin.skinWhite,
                    b.skin.skinWheat,
                    b.skin.skinDark,
                    b.hair.hairBlonde,
                    b.hair.hairBrown,
                    b.hair.hairDark,
                    b.searchButton).forEach { it.setOnClickListener(this) }
        }
    }

    override fun onStart() {
        super.onStart()
        internetConnectionDisposable = viewModel.internetConnectivityFlowable
                .subscribe(this::handleConnectionStatusChange) {
                    Timber.error(it, it::toString)
                }
    }

    private fun handleConnectionStatusChange(connected: Boolean) {
        setLocationEnabled(connected)
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

    private fun initializeEditTexts() {
        binding?.let { b ->

            b.name.firstNameEditText.setText(viewModel.firstName.value)
            b.name.lastNameEditText.setText(viewModel.lastName.value)

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

    private fun initializeNumberPickers() {
        binding?.let { b ->
            b.ageNumberPicker.value = viewModel.age.value ?: 15
            b.heightNumberPicker.value = viewModel.height.value ?: 120
            b.ageNumberPicker.setOnValueChangedListener { _, _, newVal -> viewModel.age.value = newVal }
            b.heightNumberPicker.setOnValueChangedListener { _, _, newVal -> viewModel.height.value = newVal }
        }
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

    private fun search() {
        viewModel.toChildQuery()?.let {
            findNavController().navigate(
                    FindChildrenFragmentDirections
                            .actionFindChildrenFragmentToChildrenSearchResultsFragment(it.bundle(ChildQuery.serializer()))
            )
        }
    }

    private fun startPlacePicker() {
        setLocationEnabled(false)
        placePicker.get().start(checkNotNull(activity)) {
            setLocationEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    private fun setLocationEnabled(enabled: Boolean) {
        binding?.let { b ->
            b.locationImageView.isEnabled = enabled
            b.locationTextView.isEnabled = enabled
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        setLocationEnabled(true)

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

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        internetConnectionDisposable?.dispose()
        super.onStop()
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

            R.id.search_button -> search()
        }
    }
}
