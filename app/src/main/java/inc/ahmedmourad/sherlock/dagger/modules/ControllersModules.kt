package inc.ahmedmourad.sherlock.dagger.modules

import arrow.syntax.function.partially1
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.dagger.modules.factories.*
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.*
import inc.ahmedmourad.sherlock.model.common.TaggedController
import inc.ahmedmourad.sherlock.view.controllers.HomeController
import inc.ahmedmourad.sherlock.view.controllers.auth.ResetPasswordController
import inc.ahmedmourad.sherlock.view.controllers.auth.SignInController
import inc.ahmedmourad.sherlock.view.controllers.auth.SignUpController
import inc.ahmedmourad.sherlock.view.controllers.auth.SignedInUserProfileController
import inc.ahmedmourad.sherlock.view.controllers.children.AddChildController
import inc.ahmedmourad.sherlock.view.controllers.children.FindChildrenController

@Module(includes = [
    MainActivityModule::class
])
internal object AddChildControllerModule {

    @Provides
    @AddChildControllerQualifier
    @JvmStatic
    fun provideAddChildController(): TaggedController {
        return AddChildController.newInstance()
    }

    @Provides
    @Reusable
    @AddChildControllerIntentQualifier
    @JvmStatic
    fun provideAddChildControllerIntent(activityFactory: MainActivityIntentFactory): AddChildControllerIntentFactory {
        return ::addChildControllerIntentFactory.partially1(activityFactory)
    }
}

@Module(includes = [
    MainActivityModule::class
])
internal object ChildDetailsControllerModule {

    @Provides
    @Reusable
    @JvmStatic
    fun provideChildDetailsController(): ChildDetailsControllerFactory {
        return ::childDetailsControllerFactory
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideChildDetailsControllerIntent(activityFactory: MainActivityIntentFactory): ChildDetailsControllerIntentFactory {
        return ::childDetailsControllerIntentFactory.partially1(activityFactory)
    }
}

@Module
internal object FindChildrenControllerModule {
    @Provides
    @FindChildrenControllerQualifier
    @JvmStatic
    fun provideFindChildrenController(): TaggedController {
        return FindChildrenController.newInstance()
    }
}

@Module
internal object ResetPasswordControllerModule {
    @Provides
    @ResetPasswordControllerQualifier
    @JvmStatic
    fun provideResetPasswordController(): TaggedController {
        return ResetPasswordController.newInstance()
    }
}

@Module
internal object HomeControllerModule {
    @Provides
    @HomeControllerQualifier
    @JvmStatic
    fun provideHomeController(): TaggedController {
        return HomeController.newInstance()
    }
}

@Module
internal object ChildrenSearchResultsControllerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideChildrenSearchResultsController(): ChildrenSearchResultsControllerFactory {
        return ::childrenSearchResultsControllerFactory
    }
}

@Module
internal object SignInControllerModule {
    @Provides
    @SignInControllerQualifier
    @JvmStatic
    fun provideSignInController(): TaggedController {
        return SignInController.newInstance()
    }
}

@Module
internal object SignUpControllerModule {
    @Provides
    @SignUpControllerQualifier
    @JvmStatic
    fun provideSignUpController(): TaggedController {
        return SignUpController.newInstance()
    }
}

@Module
internal object SignedInUserProfileControllerModule {
    @Provides
    @SignedInUserProfileControllerQualifier
    @JvmStatic
    fun provideSignedInUserProfileController(): TaggedController {
        return SignedInUserProfileController.newInstance()
    }
}

@Module
internal object CompleteSignUpControllerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideCompleteSignUpController(): CompleteSignUpControllerFactory {
        return ::completeSignUpControllerFactory
    }
}
