package kr.kro.dearmoment.inquiry.domain

import java.time.LocalDateTime
import java.util.UUID

class StudioInquiry(
    id: Long = 0L,
    userId: UUID,
    val title: String,
    val content: String,
    val createdDate: LocalDateTime = LocalDateTime.now(),
) : Inquiry(id, userId) {
    init {
        require(title.isNotEmpty()) { "제목이 존재해야 합니다." }
        require(content.isNotEmpty()) { "내용이 존재해야 합니다." }
    }
}
