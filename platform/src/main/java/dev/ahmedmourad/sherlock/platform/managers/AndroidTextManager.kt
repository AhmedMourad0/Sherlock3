package dev.ahmedmourad.sherlock.platform.managers

import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.TextManager
import inc.ahmedmourad.sherlock.platform.R
import splitties.init.appCtx
import javax.inject.Inject

@Reusable
internal class AndroidTextManager @Inject constructor() : TextManager {

    override fun whiteSkin(): String = appCtx.getString(R.string.white_skin)

    override fun wheatishSkin(): String = appCtx.getString(R.string.wheatish_skin)

    override fun darkSkin(): String = appCtx.getString(R.string.dark_skin)

    override fun blondeHair(): String = appCtx.getString(R.string.blonde_hair)

    override fun brownHair(): String = appCtx.getString(R.string.brown_hair)

    override fun darkHair(): String = appCtx.getString(R.string.dark_hair)

    override fun male(): String = appCtx.getString(R.string.male)

    override fun female(): String = appCtx.getString(R.string.female)
}
