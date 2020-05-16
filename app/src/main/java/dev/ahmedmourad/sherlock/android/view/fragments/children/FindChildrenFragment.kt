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
import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.databinding.FragmentFindChildrenBinding
import dev.ahmedmourad.sherlock.android.di.injector
import dev.ahmedmourad.sherlock.android.interpreters.interactors.localizedMessage
import dev.ahmedmourad.sherlock.android.pickers.colors.ColorSelector
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.utils.observe
import dev.ahmedmourad.sherlock.android.utils.observeAll
import dev.ahmedmourad.sherlock.android.utils.somethingWentWrong
import dev.ahmedmourad.sherlock.android.view.BackdropProvider
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.factory.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.FindChildrenViewModel
import dev.ahmedmourad.sherlock.android.viewmodel.shared.GlobalViewModel
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
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

        createSkinColorViews()
        createHairColorViews()
        initializeEditTexts()
        initializeGenderRadioGroup()
        initializeNumberPickers()
        initializeLocationTextView()
        addErrorObservers()

        observe(globalViewModel.internetConnectivity) { either: Either<ObserveInternetConnectivityInteractor.Exception, Boolean> ->
            when (either) {
                is Either.Left -> {
                    setInternetDependantViewsEnabled(false)
                    Timber.error(message = either.a::toString)
                }
                is Either.Right -> {
                    setInternetDependantViewsEnabled(either.b)
                }
            }.exhaust()
        }

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

    //This's temporary and is here for debugging purposes
    private fun addErrorObservers() {
        observeAll(viewModel.firstNameError,
                viewModel.lastNameError,
                viewModel.nameError,
                viewModel.locationError,
                viewModel.ageError,
                viewModel.heightError,
                viewModel.genderError,
                viewModel.skinError,
                viewModel.hairError,
                viewModel.appearanceError,
                viewModel.queryError
        ) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onFirstNameErrorDismissed()
                viewModel.onLastNameErrorDismissed()
                viewModel.onNameErrorDismissed()
                viewModel.onLocationErrorDismissed()
                viewModel.onAgeErrorDismissed()
                viewModel.onHeightErrorDismissed()
                viewModel.onGenderErrorDismissed()
                viewModel.onSkinErrorDismissed()
                viewModel.onHairErrorDismissed()
                viewModel.onAppearanceErrorDismissed()
                viewModel.onQueryErrorDismissed()
            }
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
                            ?: Skin.WHITE
            ).apply {
                onSelectionChangeListeners.add { viewModel.onSkinChange(it.value) }
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

    private fun search() {
        val signedInUserEither = globalViewModel.signedInUser.value
        if (signedInUserEither != null) {
            signedInUserEither.fold(ifLeft = {
                Toast.makeText(appCtx, it.localizedMessage(), Toast.LENGTH_LONG).show()
            }, ifRight = { user ->
                if (user == null) {
                    (requireActivity() as BackdropProvider).setInPrimaryContentMode(false)
                } else {
                    viewModel.toChildQuery()?.let {
                        findNavController().navigate(
                                FindChildrenFragmentDirections
                                        .actionFindChildrenFragmentToChildrenSearchResultsFragment(
                                                it.bundle(ChildQuery.serializer())
                                        )
                        )
                    }
                }
            })
        } else {
            Toast.makeText(appCtx, somethingWentWrong(), Toast.LENGTH_LONG).show()
            (requireActivity() as BackdropProvider).setInPrimaryContentMode(false)
        }
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

            R.id.location_image_view, R.id.location_text_view -> startPlacePicker()

            R.id.search_button -> search()
        }
    }
}
