package inc.ahmedmourad.sherlock.model.common

import android.content.Intent
import com.bluelinelabs.conductor.Router

internal interface DeeplyLinkedController {
    fun isDestination(destinationId: Int): Boolean
    fun navigate(router: Router, intent: Intent)
}
