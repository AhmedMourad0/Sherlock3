package inc.ahmedmourad.sherlock.view.controllers.children

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenSearchResultsControllerFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.FindChildrenViewModelQualifier
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.domain.model.common.disposable
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.utils.defaults.DefaultTextWatcher
import inc.ahmedmourad.sherlock.utils.pickers.colors.ColorSelector
import inc.ahmedmourad.sherlock.utils.pickers.places.PlacePicker
import inc.ahmedmourad.sherlock.utils.viewModelProvider
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.FindChildrenViewModel
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

internal class FindChildrenController : LifecycleController(), View.OnClickListener {

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

    @BindView(R.id.find_children_age_number_picker)
    internal lateinit var ageNumberPicker: NumberPicker

    @BindView(R.id.find_children_height_number_picker)
    internal lateinit var heightNumberPicker: NumberPicker

    @BindView(R.id.find_children_location_text_view)
    internal lateinit var locationTextView: MaterialTextView

    @BindView(R.id.find_children_location_image_view)
    internal lateinit var locationImageView: ImageView

    @BindView(R.id.find_children_search_button)
    internal lateinit var searchButton: MaterialButton

    @Inject
    @field:FindChildrenViewModelQualifier
    internal lateinit var viewModelFactory: ViewModelProvider.NewInstanceFactory

    @Inject
    internal lateinit var childrenSearchResultsControllerFactory: ChildrenSearchResultsControllerFactory

    @Inject
    internal lateinit var placePicker: Lazy<PlacePicker>

    private lateinit var skinColorSelector: ColorSelector<Skin>
    private lateinit var hairColorSelector: ColorSelector<Hair>

    private lateinit var viewModel: FindChildrenViewModel

    private var internetConnectionDisposable by disposable()

    private lateinit var context: Context

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.findChildrenComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_find_children, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        viewModel = viewModelProvider(viewModelFactory)[FindChildrenViewModel::class.java]

        arrayOf(::createSkinColorViews,
                ::createHairColorViews,
                ::initializeEditTexts,
                ::initializeGenderRadioGroup,
                ::initializeLocationTextView,
                ::initializeNumberPickers).forEach { it() }

        arrayOf(locationImageView,
                locationTextView,
                skinWhiteView,
                skinWheatView,
                skinDarkView,
                hairBlondView,
                hairBrownView,
                hairDarkView,
                searchButton).forEach { it.setOnClickListener(this) }

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        internetConnectionDisposable = viewModel.internetConnectivityFlowable
                .subscribe(this::handleConnectionStatusChange) {
                    Timber.error(it, it::toString)
                }
    }

    private fun handleConnectionStatusChange(connected: Boolean) {
        setLocationEnabled(connected)
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

    private fun initializeEditTexts() {

        firstNameEditText.setText(viewModel.firstName.value)
        lastNameEditText.setText(viewModel.lastName.value)

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

    private fun initializeNumberPickers() {
        ageNumberPicker.value = viewModel.age.value ?: 15
        heightNumberPicker.value = viewModel.height.value ?: 120
        ageNumberPicker.setOnValueChangedListener { _, _, newVal -> viewModel.age.value = newVal }
        heightNumberPicker.setOnValueChangedListener { _, _, newVal -> viewModel.height.value = newVal }
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

    private fun search() {
        viewModel.toChildQuery()?.let {
            val taggedController = childrenSearchResultsControllerFactory(it)
            router.pushController(RouterTransaction.with(taggedController.controller).tag(taggedController.tag))
        }
    }

    private fun startPlacePicker() {
        setLocationEnabled(false)
        placePicker.get().start(checkNotNull(activity)) {
            setLocationEnabled(true)
            Timber.error(it, it::toString)
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

    override fun onDetach(view: View) {
        internetConnectionDisposable?.dispose()
        super.onDetach(view)
    }

    override fun onDestroy() {
        internetConnectionDisposable?.dispose()
        SherlockComponent.Controllers.findChildrenComponent.release()
        unbinder.unbind()
        super.onDestroy()
    }

    private fun setLocationEnabled(enabled: Boolean) {
        locationImageView.isEnabled = enabled
        locationTextView.isEnabled = enabled
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.skin_white -> skinColorSelector.select(Skin.WHITE)

            R.id.skin_wheat -> skinColorSelector.select(Skin.WHEAT)

            R.id.skin_dark -> skinColorSelector.select(Skin.DARK)

            R.id.hair_blonde -> hairColorSelector.select(Hair.BLONDE)

            R.id.hair_brown -> hairColorSelector.select(Hair.BROWN)

            R.id.hair_dark -> hairColorSelector.select(Hair.DARK)

            R.id.find_children_location_image_view, R.id.find_children_location_text_view -> startPlacePicker()

            R.id.find_children_search_button -> search()
        }
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.FindChildrenController"

        fun newInstance() = TaggedController(FindChildrenController(), CONTROLLER_TAG)
    }
}
