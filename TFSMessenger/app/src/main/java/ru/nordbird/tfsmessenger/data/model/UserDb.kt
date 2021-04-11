package ru.nordbird.tfsmessenger.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserDb(

    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "full_name")
    val full_name: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "avatar_url")
    val avatar_url: String,

    @ColumnInfo(name = "timestamp")
    var timestamp: Int
)
