package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.user.application.dto.response.UserResponse
import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserProfileService(
    private val getUserByIdPort: GetUserByIdPort,
    private val saveUserPort: SaveUserPort
) {

    fun getProfile(userId: UUID): User {
        return getUserByIdPort.findById(userId)
            ?: throw UsernameNotFoundException("User not found with id: $userId")
    }

    fun updateName(userId: UUID, newName: String): User {
        val existingUser = getUserByIdPort.findById(userId)
            ?: throw UsernameNotFoundException("User not found with id: $userId")
        // 도메인 모델은 불변 객체일 경우 복사본을 생성합니다.
        // 필요한 경우 User에 copy() 함수를 구현하거나, 업데이트 로직을 추가합니다.
        val updatedUser = existingUser.copy(
            name = newName,
            updatedAt = LocalDateTime.now()
        )
        return saveUserPort.save(updatedUser)
    }
}
