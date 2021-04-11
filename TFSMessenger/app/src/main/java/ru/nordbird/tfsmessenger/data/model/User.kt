package ru.nordbird.tfsmessenger.data.model

data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val avatar_url: String,
    var timestamp: Int = 0
)
