package kr.kro.dearmoment.user.application.port.output

import kr.kro.dearmoment.user.domain.User
import java.util.UUID

interface GetUserByIdPort {
    fun findById(id: UUID): User?

    fun findByLoginId(loginId: String): User?
}
