package dev.ahmedmourad.sherlock.children.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import dev.ahmedmourad.sherlock.children.local.contract.Contract
import dev.ahmedmourad.sherlock.children.local.daos.SearchResultsDao
import dev.ahmedmourad.sherlock.children.local.entities.RoomChildEntity
import splitties.init.appCtx

@Database(entities = [RoomChildEntity::class], version = 1)
internal abstract class ChildrenRoomDatabase : RoomDatabase() {

    abstract fun resultsDao(): SearchResultsDao

    internal companion object {

        @Volatile
        private var INSTANCE: ChildrenRoomDatabase? = null

        fun getInstance() = INSTANCE ?: synchronized(ChildrenRoomDatabase::class.java) {
            INSTANCE ?: buildDatabase().also { INSTANCE = it }
        }

        private fun buildDatabase() = Room.databaseBuilder(
                appCtx,
                ChildrenRoomDatabase::class.java,
                Contract.DATABASE_NAME
        ).build()
    }
}
