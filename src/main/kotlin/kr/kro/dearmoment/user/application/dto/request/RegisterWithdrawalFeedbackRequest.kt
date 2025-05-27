package kr.kro.dearmoment.user.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.user.domain.WithdrawalFeedback
import kr.kro.dearmoment.user.domain.WithdrawalReason

@Schema(description = "회원 탈퇴 사유 등록 요청")
data class RegisterWithdrawalFeedbackRequest(
    @Schema(
        description = "탈퇴 사유 코드",
        example = "6",
        allowableValues = ["1", "2", "3", "4", "5", "6"]
    )
    @field:Min(1)
    @field:Max(6)
    val reasonCode: Int,
    @Schema(
        description = "기타 사유 직접 입력 (reasonCode = 6 일 때 필수)",
        example = "파혼으로 웨딩 사진 불필요",
    )
    @field:Size(max = 255)
    val customReason: String? = null
) {
    /** 조건부 검증: reasonCode == 6 ➜ customReason 필수, 그 외 ➜ 비워야 함 */
    @AssertTrue(message = "reasonCode=6 일 때 customReason 을 입력해야 합니다. 기타가 아니면 customReason 을 비워 두세요.")
    private fun isCustomReasonValid(): Boolean =
        if (reasonCode == WithdrawalReason.OTHER.code) {
            !customReason.isNullOrBlank()
        } else {
            customReason.isNullOrBlank()
        }

    // ───────── 변환 헬퍼 ─────────
    fun toDomain() =
        WithdrawalFeedback(
            reason = WithdrawalReason.from(reasonCode),
            customReason = customReason
        )
}
