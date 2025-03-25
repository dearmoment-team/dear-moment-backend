package kr.kro.dearmoment.user.application.port.output

import kr.kro.dearmoment.user.domain.User

interface SaveUserPort {
    fun save(user: User): User
}
