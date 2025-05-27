package kr.kro.dearmoment.user.adapter.output.persistence

import kr.kro.dearmoment.user.application.port.output.SaveWithdrawalFeedbackPort
import kr.kro.dearmoment.user.domain.WithdrawalFeedback
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WithdrawalFeedbackJpaAdapter(
    private val repository: WithdrawalFeedbackRepository
) : SaveWithdrawalFeedbackPort {
    @Transactional
    override fun save(feedback: WithdrawalFeedback): WithdrawalFeedback =
        repository.save(WithdrawalFeedbackEntity.from(feedback)).toDomain()
}
