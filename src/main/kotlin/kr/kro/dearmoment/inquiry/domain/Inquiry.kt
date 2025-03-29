package kr.kro.dearmoment.inquiry.domain

import java.util.UUID

sealed class Inquiry(
    val id: Long = 0L,
    val userId: UUID,
) {
    init {
        require(userId.toString().isNotEmpty()) { "유저 ID는 필수입니다." }
    }
}
