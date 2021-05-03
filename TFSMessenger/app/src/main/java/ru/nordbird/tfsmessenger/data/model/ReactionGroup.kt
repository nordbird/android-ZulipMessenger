package ru.nordbird.tfsmessenger.data.model

class ReactionGroup(
    val code: String,
    val name: String,
    val userIdList: List<Int>,
) {
    override fun toString() = "$code ${userIdList.size}"
}