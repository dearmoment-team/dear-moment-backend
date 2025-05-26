package kr.kro.dearmoment.user.domain

import java.time.LocalDateTime

data class WithdrawalFeedback(
    val id: Long? = null,
    val reason: WithdrawalReason,
    val customReason: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        if (reason == WithdrawalReason.OTHER) {
            require(!customReason.isNullOrBlank()) { "기타 사유는 입력이 필요합니다." }
        } else {
            require(customReason.isNullOrBlank()) { "기타가 아닐 때 customReason 은 비워두세요." }
        }
    }
}
