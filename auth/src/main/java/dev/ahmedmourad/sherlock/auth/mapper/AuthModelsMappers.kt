package dev.ahmedmourad.sherlock.auth.mapper

import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId

internal fun SignUpUser.toRemoteSignUpUser(id: UserId, pictureUrl: Url?): RemoteSignUpUser {
    return RemoteSignUpUser(
            id,
            credentials.email,
            Username.from(displayName),
            displayName,
            phoneNumber,
            pictureUrl
    )
}

internal fun CompletedUser.toRemoteSignUpUser(pictureUrl: Url?): RemoteSignUpUser {
    return RemoteSignUpUser(
            id,
            email,
            Username.from(displayName),
            displayName,
            phoneNumber,
            pictureUrl
    )
}
