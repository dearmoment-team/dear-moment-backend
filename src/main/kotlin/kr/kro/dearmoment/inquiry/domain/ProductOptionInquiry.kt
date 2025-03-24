package kr.kro.dearmoment.inquiry.domain

import java.time.LocalDateTime

class ProductOptionInquiry(
    id: Long,
    userId: Long,
    val productId: Long,
    val studioName: String,
    val optionName: String,
    val thumbnailUrl: String,
    val createdDate: LocalDateTime = LocalDateTime.now(),
) : Inquiry(id, userId)
