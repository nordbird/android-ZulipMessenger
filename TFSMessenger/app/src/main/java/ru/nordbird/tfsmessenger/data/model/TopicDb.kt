package ru.nordbird.tfsmessenger.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "topics", primaryKeys = ["name", "stream_id"])
data class TopicDb(

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "stream_id")
    var streamId: Int = 0
)
