package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.Attachment
import ru.nordbird.tfsmessenger.ui.recycler.holder.AttachmentUi

class AttachmentToAttachmentUiMapper(
    private val makeIncoming: Boolean
) : Mapper<List<Attachment>, List<AttachmentUi>> {

    override fun transform(data: List<Attachment>): List<AttachmentUi> {
        return data.map { AttachmentUi.makeAttachment(it.title, it.url, makeIncoming) }
    }

}