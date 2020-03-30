package inc.ahmedmourad.sherlock.auth.mapper

import inc.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import inc.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.UserId

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
