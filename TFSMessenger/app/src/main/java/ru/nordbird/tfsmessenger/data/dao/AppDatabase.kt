package ru.nordbird.tfsmessenger.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.nordbird.tfsmessenger.data.model.UserDb

@Database(
    entities = [
        UserDb::class
    ], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}