package ru.nordbird.tfsmessenger.data.dao

import androidx.room.*
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.TopicDb

@Dao
interface TopicDao {

    @Query("SELECT * FROM topics WHERE stream_id = :streamId")
    fun getByStreamId(streamId: Int): Single<List<TopicDb>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(topics: List<TopicDb>)

}