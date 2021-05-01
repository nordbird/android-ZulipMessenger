package ru.nordbird.tfsmessenger.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(tableName = "messages")
data class MessageDb(

    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "stream_name")
    val streamName: String,

    @ColumnInfo(name = "topic_name")
    val topicName: String,

    @ColumnInfo(name = "sender_id")
    val authorId: Int,

    @ColumnInfo(name = "sender_full_name")
    val authorName: String,

    @ColumnInfo(name = "avatar_url")
    val avatar_url: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp_ms: Long,

    @ColumnInfo(name = "reactions")
    val reactions: List<Reaction> = listOf(),

    @ColumnInfo(name = "local_id")
    val localId: Int = 0
)