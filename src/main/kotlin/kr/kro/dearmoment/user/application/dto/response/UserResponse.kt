package kr.kro.dearmoment.user.application.dto.response

import kr.kro.dearmoment.user.domain.User
import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID?,
    val loginId: String?,
    val name: String,
    val isStudio: Boolean?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                loginId = user.loginId, // null 가능
                name = user.name,
                isStudio = user.isStudio,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
