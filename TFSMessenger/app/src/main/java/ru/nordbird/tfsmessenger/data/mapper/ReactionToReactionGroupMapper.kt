package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.ReactionGroup

class ReactionToReactionGroupMapper : Mapper<List<Reaction>, List<ReactionGroup>> {

    override fun transform(data: List<Reaction>): List<ReactionGroup> {
        val reactionByCode = data.groupBy { it.code }
        return reactionByCode.map { (code, reactions) -> ReactionGroup(code, parseReaction(reactions)) }
    }

    private fun parseReaction(reactions: List<Reaction>): List<String> {
        return reactions.map { it.userId }
    }

}