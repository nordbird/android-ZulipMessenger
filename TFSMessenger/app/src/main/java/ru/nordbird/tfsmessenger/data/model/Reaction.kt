package ru.nordbird.tfsmessenger.data.model

class Reaction(
     val code: Int,
     val userIdList: List<String>,
) {
    override fun toString() = "$code ${userIdList.size}"
}