package kr.kro.dearmoment.user.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.user.domain.WithdrawalFeedback
import kr.kro.dearmoment.user.domain.WithdrawalReason
import java.time.LocalDateTime

@Entity
@Table(name = "user_withdrawal_reason")
class WithdrawalFeedbackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val reasonId: Long? = null,
    @Column(nullable = false)
    val reasonCode: Int,
    @Column(length = 255)
    val customReason: String? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() =
        WithdrawalFeedback(
            id = reasonId,
            reason = WithdrawalReason.from(reasonCode),
            customReason = customReason,
            createdAt = createdAt
        )

    companion object {
        fun from(d: WithdrawalFeedback) =
            WithdrawalFeedbackEntity(
                reasonId = d.id,
                reasonCode = d.reason.code,
                customReason = d.customReason,
                createdAt = d.createdAt
            )
    }
}
