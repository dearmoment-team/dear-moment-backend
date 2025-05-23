package kr.kro.dearmoment.user.application.port.input

import kr.kro.dearmoment.user.domain.User
import java.util.UUID

interface SkipProfileConsentUseCase {
    fun skipAddInfo(userId: UUID): User
}
