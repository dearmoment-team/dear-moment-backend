package kr.kro.dearmoment.user.application.port.input

import kr.kro.dearmoment.user.application.command.RegisterUserCommand
import kr.kro.dearmoment.user.application.dto.response.UserResponse

interface RegisterUserUseCase {
    fun register(command: RegisterUserCommand): UserResponse
}
