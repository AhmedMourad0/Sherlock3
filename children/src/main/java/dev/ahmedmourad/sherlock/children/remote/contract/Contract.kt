package dev.ahmedmourad.sherlock.children.remote.contract

internal object Contract {

    object Database {

        object Children {

            const val PATH = "children"

            const val USER_ID = "user_id"
            const val TIMESTAMP = "timestamp"
            const val FIRST_NAME = "first_name"
            const val LAST_NAME = "last_name"
            const val LOCATION_ID = "location_id"
            const val LOCATION_NAME = "location_name"
            const val LOCATION_ADDRESS = "location_address"
            const val LOCATION_LATITUDE = "location_latitude"
            const val LOCATION_LONGITUDE = "location_longitude"
            const val MIN_AGE = "min_age"
            const val MAX_AGE = "max_age"
            const val MIN_HEIGHT = "min_height"
            const val MAX_HEIGHT = "max_height"
            const val GENDER = "gender"
            const val SKIN = "skin"
            const val HAIR = "hair"
            const val PICTURE_URL = "picture_url"
            const val NOTES = "notes"
        }

        object Queries {

            const val PATH = "queries"

            const val PAGE = "page"
            const val TIMESTAMP = "timestamp"
            const val USER_ID = "user_id"
            const val FIRST_NAME = "first_name"
            const val LAST_NAME = "last_name"
            const val LATITUDE = "latitude"
            const val LONGITUDE = "longitude"
            const val GENDER = "gender"
            const val SKIN = "skin"
            const val HAIR = "hair"
            const val AGE = "age"
            const val HEIGHT = "height"

            object Results {

                const val PATH = "results"

                const val CHILD_ID = "child_id"
                const val WEIGHT = "weight"
            }
        }

        object Investigations {

            const val PATH = "investigations"

            const val USER_ID = "user_id"
            const val TIMESTAMP = "timestamp"
            const val FIRST_NAME = "first_name"
            const val LAST_NAME = "last_name"
            const val LATITUDE = "latitude"
            const val LONGITUDE = "longitude"
            const val GENDER = "gender"
            const val SKIN = "skin"
            const val HAIR = "hair"
            const val AGE = "age"
            const val HEIGHT = "height"
        }
    }
}
