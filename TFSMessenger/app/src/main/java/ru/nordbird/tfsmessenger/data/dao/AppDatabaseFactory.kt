package ru.nordbird.tfsmessenger.data.dao

import android.content.Context
import androidx.room.Room

object AppDatabaseFactory {

    fun create(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "tfs_messenger"
        ).build()
    }

}