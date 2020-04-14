package dev.ahmedmourad.sherlock.auth.remote.utils

import com.google.firebase.firestore.FieldValue
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.auth.remote.contract.Contract

internal fun RemoteSignUpUser.toMap(): Map<String, Any?> = hashMapOf(
        Contract.Database.Users.REGISTRATION_DATE to FieldValue.serverTimestamp(),
        Contract.Database.Users.LAST_LOGIN_DATE to FieldValue.serverTimestamp(),
        Contract.Database.Users.EMAIL to email.value,
        Contract.Database.Users.USER_NAME to username.value,
        Contract.Database.Users.DISPLAY_NAME to displayName.value,
        Contract.Database.Users.COUNTRY_CODE to phoneNumber.countryCode,
        Contract.Database.Users.PHONE_NUMBER to phoneNumber.number,
        Contract.Database.Users.PICTURE_URL to pictureUrl?.value
)
