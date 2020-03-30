package inc.ahmedmourad.sherlock.auth.dagger

import dagger.Module
import inc.ahmedmourad.sherlock.auth.dagger.modules.AuthManagerModule

@Module(includes = [AuthManagerModule::class])
object AuthModule
