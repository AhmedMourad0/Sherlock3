package dev.ahmedmourad.sherlock.children.remote.fcm

import android.annotation.SuppressLint
import arrow.core.orNull
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class BaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val id = remoteMessage.data["child_id"]?.let(::ChildId) ?: return
            val weight = remoteMessage.data["weight"]
                    ?.toDoubleOrNull()
                    ?.let(Weight.Companion::of)
                    ?.orNull() ?: return
            handleMatchingChildFound(id, weight)
        }
    }

    abstract fun handleMatchingChildFound(id: ChildId, weight: Weight)
}
