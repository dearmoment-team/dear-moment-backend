package kr.kro.dearmoment.user.application.port.output

import kr.kro.dearmoment.user.domain.WithdrawalFeedback

interface SaveWithdrawalFeedbackPort {
    fun save(feedback: WithdrawalFeedback): WithdrawalFeedback
}
