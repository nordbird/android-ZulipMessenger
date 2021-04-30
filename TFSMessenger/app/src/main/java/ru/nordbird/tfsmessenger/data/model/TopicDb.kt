package ru.nordbird.tfsmessenger.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "topics", primaryKeys = ["name", "stream_name"])
data class TopicDb(

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "stream_name")
    val streamName: String = ""
)
