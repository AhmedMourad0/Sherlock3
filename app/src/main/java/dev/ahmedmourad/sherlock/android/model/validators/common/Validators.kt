package dev.ahmedmourad.sherlock.android.model.validators.common

import arrow.core.Either
import arrow.core.right
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import splitties.init.appCtx

internal fun validatePicturePathNullable(value: String?): Either<String, PicturePath?> {
    value ?: return null.right()
    return validatePicturePath(value)
}

internal fun validatePicturePath(value: String): Either<String, PicturePath> {
    return PicturePath.of(value).mapLeft {
        when (it) {
            PicturePath.Exception.BlankPathException ->
                appCtx.getString(R.string.blank_picture_path)
            PicturePath.Exception.NonExistentFileException ->
                appCtx.getString(R.string.file_doesnt_exists_at_path)
            PicturePath.Exception.NonFilePathException ->
                appCtx.getString(R.string.path_not_a_file)
            PicturePath.Exception.NonPicturePathException ->
                appCtx.getString(R.string.path_not_a_picture)
            PicturePath.Exception.GifPathException ->
                appCtx.getString(R.string.gif_files_not_supported)
            PicturePath.Exception.UnreadableFileException ->
                appCtx.getString(R.string.unreadable_file)
            PicturePath.Exception.SecurityException ->
                appCtx.getString(R.string.no_permission_to_read_file)
        }
    }
}
