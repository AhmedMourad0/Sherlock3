package dev.ahmedmourad.sherlock.auth.remote.contract

internal object Contract {

    object Database {

        object Users {

            const val PATH = "users"

            const val TIMESTAMP = "timestamp"
            const val LAST_LOGIN_TIMESTAMP = "last_login_timestamp"
            const val EMAIL = "email"
            const val DISPLAY_NAME = "display_name"
            const val USER_NAME = "username"
            const val COUNTRY_CODE = "country_code"
            const val PHONE_NUMBER = "phone_number"
            const val PICTURE_URL = "picture_url"
        }
    }
}
