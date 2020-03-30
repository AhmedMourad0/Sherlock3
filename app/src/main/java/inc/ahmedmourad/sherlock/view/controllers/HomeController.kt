package inc.ahmedmourad.sherlock.view.controllers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.dagger.modules.factories.AppSectionsRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.AddChildControllerQualifier
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.FindChildrenControllerQualifier
import inc.ahmedmourad.sherlock.model.common.AppSection
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.view.activity.MainActivity
import inc.ahmedmourad.sherlock.view.controllers.children.AddChildController
import inc.ahmedmourad.sherlock.view.controllers.children.ChildDetailsController
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject

internal class HomeController : Controller() {

    @BindView(R.id.home_recycler)
    internal lateinit var recyclerView: RecyclerView

    @Inject
    internal lateinit var adapterFactory: AppSectionsRecyclerAdapterFactory

    @Inject
    @field:AddChildControllerQualifier
    internal lateinit var addChildController: Lazy<TaggedController>

    @Inject
    @field:FindChildrenControllerQualifier
    internal lateinit var findChildrenController: Lazy<TaggedController>

    private lateinit var context: Context
    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {

        SherlockComponent.Controllers.homeComponent.get().inject(this)

        val view = inflater.inflate(R.layout.controller_home, container, false)

        unbinder = ButterKnife.bind(this, view)

        context = view.context

        initializeRecyclerView()

        val activity = this.activity

        if (activity != null && activity.intent.hasExtra(MainActivity.EXTRA_DESTINATION_ID)) {

            val destination =
                    activity.intent.getIntExtra(MainActivity.EXTRA_DESTINATION_ID, MainActivity.INVALID_DESTINATION)

            listOf(
                    AddChildController.Companion,
                    ChildDetailsController.Companion
            ).firstOrNull {
                it.isDestination(destination)
            }?.navigate(
                    router,
                    activity.intent
            )
                    ?: IllegalStateException("Destination is not supported: $destination!").let { Timber.error(it, it::toString) }
        }

        return view
    }

    private fun initializeRecyclerView() {
        recyclerView.adapter = adapterFactory(createSectionsList()) {
            if (it == null)
                Toast.makeText(context.applicationContext, R.string.coming_soon, Toast.LENGTH_LONG).show()
            else
                router.pushController(RouterTransaction.with(it.get().controller).tag(it.get().tag))
        }
        recyclerView.layoutManager = GridLayoutManager(context, context.resources.getInteger(R.integer.home_column_count))
        recyclerView.isVerticalScrollBarEnabled = true
    }

    private fun createSectionsList() = ArrayList<AppSection>(4).apply {
        add(AppSection(context.getString(R.string.found_a_child), R.drawable.found_a_child, addChildController))
        add(AppSection(context.getString(R.string.search), R.drawable.search_child, findChildrenController))
        add(AppSection(context.getString(R.string.coming_soon), R.drawable.coming_soon, null))
        add(AppSection(context.getString(R.string.coming_soon), R.drawable.coming_soon, null))
    }

    override fun onDestroy() {
        unbinder.unbind()
        SherlockComponent.Controllers.homeComponent.release()
        super.onDestroy()
    }

    companion object {

        private const val CONTROLLER_TAG = "inc.ahmedmourad.sherlock.view.controllers.tag.HomeController"

        fun newInstance() = TaggedController(HomeController(), CONTROLLER_TAG)
    }
}
