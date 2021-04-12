package ru.nordbird.tfsmessenger.data.model

class ReactionGroup(
    val code: String,
    val name: String,
    var userIdList: List<String>,
) {
    override fun toString() = "$code ${userIdList.size}"
}