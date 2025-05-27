package kr.kro.dearmoment.user.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class WithdrawalFeedbackTest : DescribeSpec({

    describe("WithdrawalFeedback 생성") {

        context("일반 사유(OTHER 이외)이고 customReason 이 비어있으면") {
            it("정상적으로 생성된다") {
                val feedback =
                    WithdrawalFeedback(
                        id = null,
                        reason = WithdrawalReason.NO_PHOTOGRAPHER_FOUND,
                        customReason = null,
                        createdAt = LocalDateTime.now()
                    )

                feedback.reason shouldBe WithdrawalReason.NO_PHOTOGRAPHER_FOUND
                feedback.customReason shouldBe null
            }
        }

        context("기타 사유(OTHER)이고 customReason 이 채워져 있으면") {
            it("정상적으로 생성된다") {
                val feedback =
                    WithdrawalFeedback(
                        reason = WithdrawalReason.OTHER,
                        customReason = "해외 이주 예정",
                    )

                feedback.reason shouldBe WithdrawalReason.OTHER
                feedback.customReason shouldBe "해외 이주 예정"
            }
        }

        context("일반 사유인데 customReason 이 들어오면") {
            it("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    WithdrawalFeedback(
                        reason = WithdrawalReason.NO_PHOTOGRAPHER_FOUND,
                        customReason = "불필요한 입력",
                    )
                }
            }
        }

        context("기타 사유인데 customReason 이 비어 있으면") {
            it("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    WithdrawalFeedback(
                        reason = WithdrawalReason.OTHER,
                        customReason = "",
                    )
                }
            }
        }
    }
})
