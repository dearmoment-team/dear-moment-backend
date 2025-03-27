package kr.kro.dearmoment.user.application.port.output

import kr.kro.dearmoment.user.domain.User
import java.util.UUID

interface GetStudioUserPort {
    fun findStudioUserById(id: UUID): User
}