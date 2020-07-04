package dev.ahmedmourad.sherlock.android.interpreters.interactors

import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.utils.somethingWentWrong
import dev.ahmedmourad.sherlock.domain.interactors.auth.*
import dev.ahmedmourad.sherlock.domain.interactors.children.AddChildInteractor
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.interactors.children.FindLastSearchResultsInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import splitties.init.appCtx

internal fun CompleteSignUpInteractor.Exception.localizedMessage(): String {
    return when (this) {
        CompleteSignUpInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        CompleteSignUpInteractor.Exception.NoSignedInUserException ->
            appCtx.getString(R.string.authentication_needed)
        is CompleteSignUpInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is CompleteSignUpInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun ObserveCurrentUserInteractor.Exception.localizedMessage(): String {
    return when (this) {
        ObserveCurrentUserInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is ObserveCurrentUserInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is ObserveCurrentUserInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun ObserveUserAuthStateInteractor.Exception.localizedMessage(): String {
    return when (this) {
        is ObserveUserAuthStateInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SendPasswordResetEmailInteractor.Exception.localizedMessage(): String {
    return when (this) {
        is SendPasswordResetEmailInteractor.Exception.NonExistentEmailException ->
            appCtx.getString(R.string.email_non_existent)
        SendPasswordResetEmailInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SendPasswordResetEmailInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SignInInteractor.Exception.localizedMessage(): String {
    return when (this) {
        SignInInteractor.Exception.AccountDoesNotExistOrHasBeenDisabledException ->
            appCtx.getString(R.string.account_disabled_or_does_not_exist)
        SignInInteractor.Exception.WrongPasswordException ->
            appCtx.getString(R.string.wrong_email_or_password)
        SignInInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SignInInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is SignInInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SignInWithFacebookInteractor.Exception.localizedMessage(): String {
    return when (this) {
        SignInWithFacebookInteractor.Exception.AccountHasBeenDisabledException ->
            appCtx.getString(R.string.account_has_been_disabled)
        SignInWithFacebookInteractor.Exception.MalformedOrExpiredCredentialException ->
            appCtx.getString(R.string.session_has_expired)
        SignInWithFacebookInteractor.Exception.EmailAlreadyInUseException ->
            appCtx.getString(R.string.email_already_in_use)
        SignInWithFacebookInteractor.Exception.NoResponseException ->
            appCtx.getString(R.string.custom_auth_no_response)
        SignInWithFacebookInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SignInWithFacebookInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is SignInWithFacebookInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SignInWithGoogleInteractor.Exception.localizedMessage(): String {
    return when (this) {
        SignInWithGoogleInteractor.Exception.AccountHasBeenDisabledException ->
            appCtx.getString(R.string.account_has_been_disabled)
        SignInWithGoogleInteractor.Exception.MalformedOrExpiredCredentialException ->
            appCtx.getString(R.string.session_has_expired)
        SignInWithGoogleInteractor.Exception.EmailAlreadyInUseException ->
            appCtx.getString(R.string.email_already_in_use)
        SignInWithGoogleInteractor.Exception.NoResponseException ->
            appCtx.getString(R.string.custom_auth_no_response)
        SignInWithGoogleInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SignInWithGoogleInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is SignInWithGoogleInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SignInWithTwitterInteractor.Exception.localizedMessage(): String {
    return when (this) {
        SignInWithTwitterInteractor.Exception.AccountHasBeenDisabledException ->
            appCtx.getString(R.string.account_has_been_disabled)
        SignInWithTwitterInteractor.Exception.MalformedOrExpiredCredentialException ->
            appCtx.getString(R.string.session_has_expired)
        SignInWithTwitterInteractor.Exception.EmailAlreadyInUseException ->
            appCtx.getString(R.string.email_already_in_use)
        SignInWithTwitterInteractor.Exception.NoResponseException ->
            appCtx.getString(R.string.custom_auth_no_response)
        SignInWithTwitterInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SignInWithTwitterInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is SignInWithTwitterInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SignOutInteractor.Exception.localizedMessage(): String {
    return when (this) {
        SignOutInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SignOutInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is SignOutInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun SignUpInteractor.Exception.localizedMessage(): String {
    return when (this) {
        SignUpInteractor.Exception.WeakPasswordException ->
            appCtx.getString(R.string.weak_password)
        SignUpInteractor.Exception.MalformedEmailException ->
            appCtx.getString(R.string.malformed_email)
        is SignUpInteractor.Exception.EmailAlreadyInUseException ->
            appCtx.getString(R.string.email_already_in_use)
        SignUpInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        is SignUpInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is SignUpInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun AddChildInteractor.Exception.localizedMessage(): String {
    return when (this) {
        AddChildInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        AddChildInteractor.Exception.NoSignedInUserException ->
            appCtx.getString(R.string.authentication_needed)
        is AddChildInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is AddChildInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun FindChildInteractor.Exception.localizedMessage(): String {
    return when (this) {
        FindChildInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        FindChildInteractor.Exception.NoSignedInUserException ->
            appCtx.getString(R.string.authentication_needed)
        is FindChildInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is FindChildInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun FindChildrenInteractor.Exception.localizedMessage(): String {
    return when (this) {
        FindChildrenInteractor.Exception.NoInternetConnectionException ->
            appCtx.getString(R.string.internet_connection_needed)
        FindChildrenInteractor.Exception.NoSignedInUserException ->
            appCtx.getString(R.string.authentication_needed)
        is FindChildrenInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun FindLastSearchResultsInteractor.Exception.localizedMessage(): String {
    return when (this) {
        is FindLastSearchResultsInteractor.Exception.InternalException ->
            somethingWentWrong(this.origin)
        is FindLastSearchResultsInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}

internal fun ObserveInternetConnectivityInteractor.Exception.localizedMessage(): String {
    return when (this) {
        is ObserveInternetConnectivityInteractor.Exception.UnknownException ->
            somethingWentWrong(this.origin)
    }
}
