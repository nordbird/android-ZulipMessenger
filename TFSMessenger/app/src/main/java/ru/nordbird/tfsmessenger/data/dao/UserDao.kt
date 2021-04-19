package ru.nordbird.tfsmessenger.data.dao

import androidx.room.*
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.UserDb

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE full_name LIKE '%' || :query || '%'")
    fun getAll(query: String = ""): Single<List<UserDb>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getById(userId: Int): Single<UserDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<UserDb>)

}