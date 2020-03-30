package inc.ahmedmourad.sherlock.children.local.contract

internal object Contract {

    const val DATABASE_NAME = "SherlockDatabase"

    object ChildrenEntry {

        const val TABLE_NAME = "children"

        const val COLUMN_ID = "id"
        const val COLUMN_PUBLICATION_DATE = "publicationDate"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_PICTURE_URL = "picture_url"
        const val COLUMN_LOCATION_ID = "location_id"
        const val COLUMN_LOCATION_NAME = "location_name"
        const val COLUMN_LOCATION_ADDRESS = "location_address"
        const val COLUMN_LOCATION_LATITUDE = "location_latitude"
        const val COLUMN_LOCATION_LONGITUDE = "location_longitude"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_GENDER = "isSameGender"
        const val COLUMN_SKIN = "isSameSkin"
        const val COLUMN_HAIR = "isSameHair"
        const val COLUMN_MIN_AGE = "min_age"
        const val COLUMN_MAX_AGE = "max_age"
        const val COLUMN_MIN_HEIGHT = "min_height"
        const val COLUMN_MAX_HEIGHT = "max_height"
        const val COLUMN_WEIGHT = "weight"
    }
}
