package inc.ahmedmourad.sherlock.dagger.modules.factories

import android.content.Intent
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.view.controllers.auth.CompleteSignUpController
import inc.ahmedmourad.sherlock.view.controllers.children.AddChildController
import inc.ahmedmourad.sherlock.view.controllers.children.ChildDetailsController
import inc.ahmedmourad.sherlock.view.controllers.children.ChildrenSearchResultsController

internal typealias AddChildControllerIntentFactory =
        (@JvmSuppressWildcards AppPublishedChild) -> @JvmSuppressWildcards Intent

internal fun addChildControllerIntentFactory(activityFactory: MainActivityIntentFactory, child: AppPublishedChild): Intent {
    return AddChildController.createIntent(activityFactory, child)
}

internal typealias ChildDetailsControllerFactory =
        (@JvmSuppressWildcards SimpleRetrievedChild) -> @JvmSuppressWildcards TaggedController

internal fun childDetailsControllerFactory(child: SimpleRetrievedChild): TaggedController {
    return ChildDetailsController.newInstance(child)
}

internal typealias ChildDetailsControllerIntentFactory =
        (@JvmSuppressWildcards SimpleRetrievedChild) -> @JvmSuppressWildcards Intent

internal fun childDetailsControllerIntentFactory(activityFactory: MainActivityIntentFactory, child: SimpleRetrievedChild): Intent {
    return ChildDetailsController.createIntent(activityFactory, child)
}

internal typealias ChildrenSearchResultsControllerFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards TaggedController

internal fun childrenSearchResultsControllerFactory(query: ChildQuery): TaggedController {
    return ChildrenSearchResultsController.newInstance(query)
}

internal typealias CompleteSignUpControllerFactory =
        (@JvmSuppressWildcards IncompleteUser) -> @JvmSuppressWildcards TaggedController

internal fun completeSignUpControllerFactory(incompleteUser: IncompleteUser): TaggedController {
    return CompleteSignUpController.newInstance(incompleteUser)
}

