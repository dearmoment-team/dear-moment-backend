package kr.kro.dearmoment.user.application.port.output

import kr.kro.dearmoment.user.domain.User
import java.util.*

interface GetUserByIdPort {
    fun findById(Id: UUID): User?

    fun findByLoginId(loginId: String): User?
}
