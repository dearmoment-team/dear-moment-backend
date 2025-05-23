package kr.kro.dearmoment.user.application.port.input

import kr.kro.dearmoment.user.application.command.AgreeProfileConsentCommand
import kr.kro.dearmoment.user.domain.User
import java.util.UUID

interface AgreeProfileConsentUseCase {
    fun agreeAddInfo(
        userId: UUID,
        cmd: AgreeProfileConsentCommand
    ): User
}
