package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class Inquiry(
    val inquiryId: Long,
    val productId: Long,
    val userName: String,
    val userContact: String,
    val inquiryDate: LocalDateTime,
    val selectedOptions: List<String> = emptyList(),
    val totalPrice: UInt,
    val inquiryContent: String? = null,
)
