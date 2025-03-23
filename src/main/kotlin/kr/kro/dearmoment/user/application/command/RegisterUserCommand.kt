package kr.kro.dearmoment.user.application.command

import java.time.LocalDateTime

data class RegisterUserCommand(
    val loginId: String,
    val password: String,
    val name: String,
) {
    val createdAt: LocalDateTime = LocalDateTime.now()
}
