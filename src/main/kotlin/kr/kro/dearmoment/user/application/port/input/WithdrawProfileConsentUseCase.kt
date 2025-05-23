package kr.kro.dearmoment.user.application.port.input

import java.util.UUID

interface WithdrawProfileConsentUseCase {
    fun withdrawAddInfo(userId: UUID)
}
