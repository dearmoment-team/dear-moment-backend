package kr.kro.dearmoment.user.adapter.output.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.user.domain.WithdrawalFeedback
import kr.kro.dearmoment.user.domain.WithdrawalReason
import java.time.LocalDateTime

@RepositoryTest
class WithdrawalFeedbackJpaAdapterTest(
    private val withdrawalFeedbackJpaRepository: WithdrawalFeedbackRepository,
) : DescribeSpec({

        val adapter = WithdrawalFeedbackJpaAdapter(withdrawalFeedbackJpaRepository)

        fun feedbackFixture(
            reason: WithdrawalReason = WithdrawalReason.NO_PHOTOGRAPHER_FOUND,
            customReason: String? = null,
            createdAt: LocalDateTime = LocalDateTime.now(),
            id: Long? = null,
        ) = WithdrawalFeedback(
            id = id,
            reason = reason,
            customReason = customReason,
            createdAt = createdAt,
        )

        describe("WithdrawalFeedbackJpaAdapter") {

            context("save 를 호출하면") {
                it("엔티티가 저장되고 PK 가 부여된다") {
                    val saved = adapter.save(feedbackFixture())
                    saved.id shouldNotBe null
                }
            }
        }
    })
