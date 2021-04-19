package ru.nordbird.tfsmessenger.data.dao

import androidx.room.*
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.StreamDb

@Dao
interface StreamDao {

    @Query("SELECT * FROM streams WHERE name LIKE '%' || :query || '%'")
    fun getStreams(query: String = ""): Single<List<StreamDb>>

    @Query("SELECT * FROM streams WHERE subscribed = 1 and name LIKE '%' || :query || '%'")
    fun getSubscriptions(query: String = ""): Single<List<StreamDb>>

    @Query("SELECT * FROM streams WHERE id = :streamId")
    fun getById(streamId: Int): Single<StreamDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(streams: List<StreamDb>)

}