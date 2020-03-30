package inc.ahmedmourad.sherlock.auth.remote.contract

internal object Contract {

    object Database {

        object Users {

            const val PATH = "u"

            const val REGISTRATION_DATE = "rd"
            const val LAST_LOGIN_DATE = "ll"
            const val EMAIL = "e"
            const val DISPLAY_NAME = "dn"
            const val USER_NAME = "un"
            const val COUNTRY_CODE = "cc"
            const val PHONE_NUMBER = "pn"
            const val PICTURE_URL = "pu"
        }
    }
}
