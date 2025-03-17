package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.user.application.command.RegisterUserCommand
import kr.kro.dearmoment.user.application.dto.response.UserResponse
import kr.kro.dearmoment.user.application.port.input.RegisterUserUseCase
import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import org.springframework.security.crypto.password.PasswordEncoder
import kr.kro.dearmoment.user.domain.User
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserCommandService(
    private val saveUserPort: SaveUserPort,
    private val getUserByLoginIdPort: GetUserByIdPort,
    private val passwordEncoder: PasswordEncoder // PasswordEncoder 주입
) : RegisterUserUseCase {

    override fun register(command: RegisterUserCommand): UserResponse {
        // 1. 중복 체크
        val existing = getUserByLoginIdPort.findByLoginId(command.loginId)
        if (existing != null) {
            throw IllegalArgumentException("이미 존재하는 loginId 입니다.")
        }

        // 2. 비밀번호 해싱
        val hashedPassword = passwordEncoder.encode(command.password)

        // 3. 도메인 객체 생성
        val user = User(
            id = null,
            loginId = command.loginId,
            password = hashedPassword,  // 해싱된 비밀번호 사용
            name = command.name,
            isStudio = true,  // 초기값
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )

        // 4. 저장
        val saved = saveUserPort.save(user)

        // 5. Response 반환
        return UserResponse(
            id = saved.id,
            loginId = saved.loginId,
            name = saved.name,
            isStudio = saved.isStudio,
            createdAt = saved.createdAt,
            updatedAt = saved.updatedAt
        )
    }
}
