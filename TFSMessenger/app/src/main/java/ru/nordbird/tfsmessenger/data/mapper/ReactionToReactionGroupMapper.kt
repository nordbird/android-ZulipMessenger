package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.ReactionGroup

class ReactionToReactionGroupMapper : Mapper<List<Reaction>, List<ReactionGroup>> {

    override fun transform(data: List<Reaction>): List<ReactionGroup> {
        val reactionByCode = data.groupBy { it.code }
        return reactionByCode.map { (code, reactions) -> ReactionGroup(getReaction(code), reactions.first().name, parseReaction(reactions)) }
    }

    private fun parseReaction(reactions: List<Reaction>): List<String> {
        return reactions.map { it.userId.toString() }
    }

    private fun getReaction(unicode: String): String {
        return String(Character.toChars(unicode.toInt(16)))
    }
}