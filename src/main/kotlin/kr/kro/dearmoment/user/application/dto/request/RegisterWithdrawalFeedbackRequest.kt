package kr.kro.dearmoment.user.application.dto.request

import kr.kro.dearmoment.user.domain.WithdrawalFeedback
import kr.kro.dearmoment.user.domain.WithdrawalReason

data class RegisterWithdrawalFeedbackRequest(
    val reasonCode: Int,
    val customReason: String? = null
) {
    fun toDomain() =
        WithdrawalFeedback(
            reason = WithdrawalReason.from(reasonCode),
            customReason = customReason
        )
}
