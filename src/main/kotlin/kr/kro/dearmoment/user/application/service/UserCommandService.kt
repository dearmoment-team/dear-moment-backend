package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.user.application.command.RegisterUserCommand
import kr.kro.dearmoment.user.application.dto.response.UserResponse
import kr.kro.dearmoment.user.application.port.input.RegisterUserUseCase
import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserCommandService(
    private val saveUserPort: SaveUserPort,
    private val getUserByLoginIdPort: GetUserByIdPort,
    // PasswordEncoder 주입
    private val passwordEncoder: PasswordEncoder,
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
        val user =
            User(
                id = null,
                loginId = command.loginId,
                // 해싱된 비밀번호 사용
                password = hashedPassword,
                name = command.name,
                // 초기값
                isStudio = true,
                createdAt = LocalDateTime.now(),
                updatedAt = null,
                // 이메일 가입이므로 null 추후 필수 가능성 있음
                kakaoId = null,
            )

        // 4. 저장
        val saved = saveUserPort.save(user)

        // 5. Response 반환
        return UserResponse.from(saved)
    }
}
