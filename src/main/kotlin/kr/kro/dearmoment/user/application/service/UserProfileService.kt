package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import kr.kro.dearmoment.user.application.dto.request.UpdateUserRequest
import kr.kro.dearmoment.user.application.dto.response.UserStudioResponse
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
    private val saveUserPort: SaveUserPort,
    private val getStudioPort: GetStudioPort,
) {
    fun getProfile(userId: UUID): UserStudioResponse {
        val user =
            getUserByIdPort.findById(userId)
                ?: throw UsernameNotFoundException("User not found with id: $userId")

        val studioId = getStudioPort.findByUserId(userId)?.id ?: 0L

        return UserStudioResponse.from(user, studioId)
    }

    fun updateUser(
        userId: UUID,
        req: UpdateUserRequest
    ): User {
        val existing =
            getUserByIdPort.findById(userId)
                ?: throw UsernameNotFoundException("User not found: $userId")

        val now = LocalDateTime.now()
        val updated =
            existing.updateProfile(
                newName = req.name,
                newIsStudio = req.isStudio,
                newBirthDate = req.birthDate,
                newSex = req.sex,
                now = now
            )
        return saveUserPort.save(updated)
    }
}
