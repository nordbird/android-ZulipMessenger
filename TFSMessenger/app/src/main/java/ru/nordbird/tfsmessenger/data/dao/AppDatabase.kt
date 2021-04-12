package ru.nordbird.tfsmessenger.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.nordbird.tfsmessenger.data.model.StreamDb
import ru.nordbird.tfsmessenger.data.model.TopicDb
import ru.nordbird.tfsmessenger.data.model.UserDb

@Database(
    entities = [
        UserDb::class,
        StreamDb::class,
        TopicDb::class
    ], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun streamDao(): StreamDao

    abstract fun topicDao(): TopicDao
}