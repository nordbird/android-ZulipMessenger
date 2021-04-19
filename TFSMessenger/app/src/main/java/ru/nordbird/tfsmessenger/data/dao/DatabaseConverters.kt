package ru.nordbird.tfsmessenger.data.dao

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nordbird.tfsmessenger.data.model.Reaction

class DatabaseConverters {

    @TypeConverter
    fun fromReactionList(value: List<Reaction>) = Json.encodeToString(value)

    @TypeConverter
    fun toReactionList(value: String) = Json.decodeFromString<List<Reaction>>(value)
}