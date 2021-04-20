package ru.nordbird.tfsmessenger.data.dao

import androidx.room.Room
import ru.nordbird.tfsmessenger.App

object AppDatabaseImpl {
    private var db: AppDatabase = Room.databaseBuilder(
        App.applicationContext(),
        AppDatabase::class.java, "tfs_messenger"
    ).build()

    fun getApi(): AppDatabase = db

    fun messageDao(): MessageDao = db.messageDao()
}