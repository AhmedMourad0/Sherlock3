package inc.ahmedmourad.sherlock.utils

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

fun FragmentActivity.findNavController(@IdRes viewId: Int): NavController {
    val navHostFragment = this.supportFragmentManager.findFragmentById(viewId) as NavHostFragment
    return navHostFragment.navController
}
