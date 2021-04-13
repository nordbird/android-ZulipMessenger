package ru.nordbird.tfsmessenger.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.nordbird.tfsmessenger.data.model.MessageDb
import ru.nordbird.tfsmessenger.data.model.StreamDb
import ru.nordbird.tfsmessenger.data.model.TopicDb
import ru.nordbird.tfsmessenger.data.model.UserDb

@Database(
    entities = [
        UserDb::class,
        StreamDb::class,
        TopicDb::class,
        MessageDb::class
    ], version = 1
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun streamDao(): StreamDao

    abstract fun topicDao(): TopicDao

    abstract fun messageDao(): MessageDao
}