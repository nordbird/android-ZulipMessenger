package ru.nordbird.tfsmessenger.data.model

class ReactionGroup(
        val code: Int,
        var userIdList: List<String>,
) {
    override fun toString() = "$code ${userIdList.size}"
}