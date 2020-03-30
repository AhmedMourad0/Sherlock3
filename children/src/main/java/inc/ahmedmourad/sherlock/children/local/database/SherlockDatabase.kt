package inc.ahmedmourad.sherlock.children.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import inc.ahmedmourad.sherlock.children.local.contract.Contract
import inc.ahmedmourad.sherlock.children.local.daos.SearchResultsDao
import inc.ahmedmourad.sherlock.children.local.entities.RoomChildEntity
import splitties.init.appCtx

@Database(entities = [RoomChildEntity::class], version = 1)
internal abstract class SherlockDatabase : RoomDatabase() {

    abstract fun resultsDao(): SearchResultsDao

    internal companion object {

        @Volatile
        private var INSTANCE: SherlockDatabase? = null

        fun getInstance() = INSTANCE ?: synchronized(SherlockDatabase::class.java) {
            INSTANCE ?: buildDatabase().also { INSTANCE = it }
        }

        private fun buildDatabase() = Room.databaseBuilder(
                appCtx,
                SherlockDatabase::class.java,
                Contract.DATABASE_NAME
        ).build()
    }
}
