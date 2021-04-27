package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi
import java.util.*

class MessageUiToViewTypedMapper : Mapper<List<MessageUi>, List<ViewTyped>> {

    override fun transform(data: List<MessageUi>): List<ViewTyped> {
        val messageByDate = data.asReversed().groupBy { Date(it.timestamp_ms).toZeroTime() }
        return messageByDate.flatMap { (date, list) -> list + SeparatorDateUi(date) }
    }

}