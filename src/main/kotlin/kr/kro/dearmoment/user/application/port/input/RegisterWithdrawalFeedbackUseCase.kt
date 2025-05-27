package kr.kro.dearmoment.user.application.port.input

import kr.kro.dearmoment.user.domain.WithdrawalFeedback

interface RegisterWithdrawalFeedbackUseCase {
    fun register(feedback: WithdrawalFeedback): WithdrawalFeedback
}
