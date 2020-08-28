package dev.ahmedmourad.sherlock.children.local.database

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dev.ahmedmourad.sherlock.children.local.ChildrenDatabase
import splitties.init.appCtx

@Volatile
private var DRIVER_INSTANCE: SqlDriver? = null

@Volatile
private var DATABASE_INSTANCE: ChildrenDatabase? = null

fun childrenDatabase(driver: SqlDriver) = DATABASE_INSTANCE
        ?: synchronized(ChildrenDatabase::class) {
            DATABASE_INSTANCE ?: buildDatabase(driver).also { DATABASE_INSTANCE = it }
        }

fun sqliteDriver() = DRIVER_INSTANCE ?: synchronized(SqlDriver::class) {
    DRIVER_INSTANCE ?: buildDriver().also { DRIVER_INSTANCE = it }
}

private fun buildDriver(): SqlDriver {
    return AndroidSqliteDriver(ChildrenDatabase.Schema, appCtx, "children.db")
}

private fun buildDatabase(driver: SqlDriver): ChildrenDatabase {
    ChildrenDatabase.Schema.create(driver)
    return ChildrenDatabase(driver)
}
