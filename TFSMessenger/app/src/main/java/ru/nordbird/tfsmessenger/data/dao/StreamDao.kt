package ru.nordbird.tfsmessenger.data.dao

import androidx.room.*
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.StreamDb

@Dao
interface StreamDao {

    @Query("SELECT * FROM streams")
    fun getStreams(): Single<List<StreamDb>>

    @Query("SELECT * FROM streams WHERE subscribed = 1")
    fun getSubscriptions(): Single<List<StreamDb>>

    @Query("SELECT * FROM streams WHERE id = :streamId")
    fun getById(streamId: Int): Single<StreamDb>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStreams(streams: List<StreamDb>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubscriptions(streams: List<StreamDb>)

}