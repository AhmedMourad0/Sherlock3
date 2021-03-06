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
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentFindChildrenBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.pickers.colors.ColorSelector
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.utils.somethingWentWrong
import dev.ahmedmourad.sherlock.android.view.BackdropActivity
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.FindChildrenViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class FindChildrenFragment : Fragment(R.layout.fragment_find_children), View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactory: Provider<AssistedViewModelFactory<FindChildrenViewModel>>

    @Inject
    internal lateinit var placePicker: Lazy<PlacePicker>

    private lateinit var skinColorSelector: ColorSelector<Skin>
    private lateinit var hairColorSelector: ColorSelector<Hair>

    private val globalViewModel: GlobalViewModel by activityViewModels()
    private val viewModel: FindChildrenViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
                this,
                viewModelFactory,
                FindChildrenViewModel.defaultArgs()
        )
    }

    private var binding: FragmentFindChildrenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFindChildrenBinding.bind(view)
        (requireActivity() as BackdropActivity).setTitle(getString(R.string.search))

        createSkinColorViews()
        createHairColorViews()
        initializeEditTexts()
        initializeGenderRadioGroup()
        initializeNumberPickers()
        initializeLocationTextView()
        addErrorObservers()
        addOpacityObservers()

        observe(globalViewModel.internetConnectivityState, Observer { state ->
            when (state) {
                GlobalViewModel.InternetConnectivityState.Connected -> {
                    setInternetDependantViewsEnabled(true)
                }
                GlobalViewModel.InternetConnectivityState.Disconnected -> {
                    setInternetDependantViewsEnabled(false)
                }
                GlobalViewModel.InternetConnectivityState.Loading -> Unit
                GlobalViewModel.InternetConnectivityState.Error -> {
                    setInternetDependantViewsEnabled(false)
                }
            }.exhaust()
        })

        binding?.let { b ->
            arrayOf(b.locationImageView,
                    b.location,
                    b.skin.skinWhite,
                    b.skin.skinWheat,
                    b.skin.skinDark,
                    b.hair.hairBlonde,
                    b.hair.hairBrown,
                    b.hair.hairDark,
                    b.searchButton
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
                viewModel.locationError,
                viewModel.ageError,
                viewModel.heightError,
                viewModel.genderError,
                viewModel.skinError,
                viewModel.hairError,
                viewModel.appearanceError,
                viewModel.queryError, observer = Observer { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onUserErrorHandled()
                viewModel.onFirstNameErrorHandled()
                viewModel.onLastNameErrorHandled()
                viewModel.onNameErrorHandled()
                viewModel.onLocationErrorHandled()
                viewModel.onAgeErrorHandled()
                viewModel.onHeightErrorHandled()
                viewModel.onGenderErrorHandled()
                viewModel.onSkinErrorHandled()
                viewModel.onHairErrorHandled()
                viewModel.onAppearanceErrorHandled()
                viewModel.onQueryErrorHandled()
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

            observe(viewModel.age, Observer {
                b.ageNumberPicker.alpha = if (it == null) 0.6f else 1f
                b.ageTextView.alpha = if (it == null) 0.6f else 1f
            })

            observe(viewModel.height, Observer {
                b.heightNumberPicker.alpha = if (it == null) 0.6f else 1f
                b.heightTextView.alpha = if (it == null) 0.6f else 1f
            })
        }
    }

    private fun setInternetDependantViewsEnabled(enabled: Boolean) {
        setLocationEnabled(enabled)
    }

    private fun createSkinColorViews() {
        binding?.let { b ->
            skinColorSelector = ColorSelector(
                    ColorSelector.newItem(Skin.WHITE, b.skin.skinWhite, R.color.colorSkinWhite),
                    ColorSelector.newItem(Skin.WHEAT, b.skin.skinWheat, R.color.colorSkinWheat),
                    ColorSelector.newItem(Skin.DARK, b.skin.skinDark, R.color.colorSkinDark),
                    default = viewModel.skin.value?.let { findEnum(it, Skin.values()) }
            ).apply {
                onSelectionChangeListeners.add { viewModel.onSkinChange(it?.value) }
            }
        }
    }

    private fun createHairColorViews() {
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

            b.name.firstNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onFirstNameChange(text.toString())
            }

            b.name.lastNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.onLastNameChange(text.toString())
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

    private fun initializeNumberPickers() {
        binding?.let { b ->

            b.ageNumberPicker.value = viewModel.age.value ?: 15
            b.heightNumberPicker.value = viewModel.height.value ?: 120

            b.ageNumberPicker.setOnValueChangedListener { _, _, newVal ->
                viewModel.onAgeChange(newVal)
            }

            b.heightNumberPicker.setOnValueChangedListener { _, _, newVal ->
                viewModel.onHeightChange(newVal)
            }

            b.ageNumberPicker.setOnClickListener {
                viewModel.onAgeChange(b.ageNumberPicker.value)
            }

            b.heightNumberPicker.setOnClickListener {
                viewModel.onHeightChange(b.heightNumberPicker.value)
            }

            b.ageNumberPicker.setOnScrollListener { _, _ ->
                viewModel.onAgeChange(b.ageNumberPicker.value)
            }

            b.heightNumberPicker.setOnScrollListener { _, _ ->
                viewModel.onHeightChange(b.heightNumberPicker.value)
            }
        }
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

    private fun search() {
        when (globalViewModel.userState.value) {

            is GlobalViewModel.UserState.Authenticated -> {
                viewModel.toChildQuery(globalViewModel.signedInUserSimplified)
                        ?.bundle(ChildrenQuery.serializer())
                        ?.let {
                            findNavController().navigate(
                                    FindChildrenFragmentDirections
                                            .actionFindChildrenFragmentToChildrenSearchResultsFragment(it)
                            )
                        }
            }

            is GlobalViewModel.UserState.Incomplete -> {
                Toast.makeText(appCtx, R.string.authentication_completion_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
            }

            GlobalViewModel.UserState.Unauthenticated -> {
                Toast.makeText(appCtx, R.string.authentication_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(false)
            }

            GlobalViewModel.UserState.Loading -> {
                Toast.makeText(appCtx, R.string.authentication_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
            }

            GlobalViewModel.UserState.NoInternet -> {
                Toast.makeText(appCtx, R.string.internet_connection_needed, Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
            }

            GlobalViewModel.UserState.Error -> {
                Toast.makeText(appCtx, somethingWentWrong(), Toast.LENGTH_LONG).show()
                (requireActivity() as BackdropActivity).setInPrimaryContentMode(true)
            }

            null -> {
                Toast.makeText(appCtx, somethingWentWrong(), Toast.LENGTH_LONG).show()
            }
        }.exhaust()
    }

    private fun startPlacePicker() {
        setLocationEnabled(false)
        placePicker.get().start(this) {
            setLocationEnabled(true)
            Timber.error(it, it::toString)
        }
    }

    private fun setLocationEnabled(enabled: Boolean) {
        binding?.let { b ->
            b.locationImageView.isEnabled = enabled
            b.location.isEnabled = enabled
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        setLocationEnabled(true)

        if (resultCode != RESULT_OK) {
            return
        }

        checkNotNull(data) {
            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            "Parameter data is null!"
        }

        placePicker.get().handleActivityResult(requestCode, data, viewModel::onLocationChange)

        super.onActivityResult(requestCode, resultCode, data)
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

            R.id.search_button -> search()
        }
    }
}
