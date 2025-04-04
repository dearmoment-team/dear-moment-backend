package kr.kro.dearmoment.user.application.port.output

import java.util.UUID

interface DeleteUserByIdPort {
    fun deleteUser(id: UUID)
}
