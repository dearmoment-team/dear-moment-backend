package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.user.application.port.input.RegisterWithdrawalFeedbackUseCase
import kr.kro.dearmoment.user.application.port.output.SaveWithdrawalFeedbackPort
import kr.kro.dearmoment.user.domain.WithdrawalFeedback
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterWithdrawalFeedbackService(
    private val savePort: SaveWithdrawalFeedbackPort
) : RegisterWithdrawalFeedbackUseCase {
    @Transactional
    override fun register(feedback: WithdrawalFeedback): WithdrawalFeedback = savePort.save(feedback)
}
