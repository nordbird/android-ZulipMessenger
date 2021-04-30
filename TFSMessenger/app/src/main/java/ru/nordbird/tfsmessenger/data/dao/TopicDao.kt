package ru.nordbird.tfsmessenger.data.dao

import androidx.room.*
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.TopicDb

@Dao
interface TopicDao {

    @Query("SELECT * FROM topics WHERE stream_name = :streamName")
    fun getByStreamName(streamName: String): Single<List<TopicDb>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(topics: List<TopicDb>)

}