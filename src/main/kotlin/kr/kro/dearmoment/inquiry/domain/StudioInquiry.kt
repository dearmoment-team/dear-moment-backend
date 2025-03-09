package kr.kro.dearmoment.inquiry.domain

import java.time.LocalDateTime

class StudioInquiry(
    id: Long = 0L,
    userId: Long = 0L,
    val title: String,
    val content: String,
    val createdDate: LocalDateTime = LocalDateTime.now(),
) : Inquiry(id, userId) {
    init {
        require(title.isNotEmpty()) { "제목이 존재해야 합니다." }
        require(content.isNotEmpty()) { "내용이 존재해야 합니다." }
    }
}
