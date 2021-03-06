package dev.ahmedmourad.sherlock.android.utils

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment

internal fun FragmentActivity.findNavController(@IdRes viewId: Int): NavController {
    val navHostFragment = this.supportFragmentManager.findFragmentById(viewId) as NavHostFragment
    return navHostFragment.navController
}

internal fun clearBackStack(navController: NavController): NavOptions {
    return NavOptions.Builder().setLaunchSingleTop(true).run {
        val backstackHead = navController.currentBackStackEntry?.destination?.id
        if (backstackHead != null) {
            setPopUpTo(backstackHead, true)
        }
        build()
    }
}
