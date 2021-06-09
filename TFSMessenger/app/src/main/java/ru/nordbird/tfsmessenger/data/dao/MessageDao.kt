package ru.nordbird.tfsmessenger.data.dao

import androidx.room.*
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.MessageDb

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE stream_name = :streamName and (id <= :lastMessageId or :lastMessageId = 0) ORDER BY id DESC LIMIT :count")
    fun getStreamMessages(streamName: String, lastMessageId: Int, count: Int): Single<List<MessageDb>>

    @Query("SELECT * FROM messages WHERE stream_name = :streamName and topic_name = :topicName and (id <= :lastMessageId or :lastMessageId = 0) ORDER BY id DESC LIMIT :count")
    fun getTopicMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Single<List<MessageDb>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    fun getById(messageId: Int): Single<MessageDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<MessageDb>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: MessageDb)

    @Query("DELETE FROM messages WHERE id = :messageId")
    fun deleteById(messageId: Int)

    @Query("DELETE FROM messages where id NOT IN (SELECT id from messages WHERE stream_name = :streamName and topic_name = :topicName ORDER BY id DESC LIMIT :limit)")
    fun deleteOverLimit(streamName: String, topicName: String, limit: Int)
}