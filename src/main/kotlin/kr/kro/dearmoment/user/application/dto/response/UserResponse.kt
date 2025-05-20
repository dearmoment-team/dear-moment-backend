package kr.kro.dearmoment.user.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.user.domain.Sex
import kr.kro.dearmoment.user.domain.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    @Schema(description = "유저 UUID", example = "a5e4565a-6bf9-403a-aec2-38883f1bccf6")
    val id: UUID?,
    @Schema(description = "로그인 ID(소셜 로그인의 경우 null)", example = "testUser123")
    val loginId: String?,
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,
    @Schema(description = "스튜디오 유저 여부", example = "true")
    val isStudio: Boolean?,
    @Schema(description = "출생 연도", example = "1995-03-03")
    val birthDate: LocalDate?,
    @Schema(description = "성별", example = "female")
    val sex: Sex?,
    @Schema(description = "초기 입력 정보 스킵 유무(입력 완료시에도 true)", example = "true")
    val addInfoIsSkip: Boolean?,
    @Schema(description = "생성 시각(UTC)", example = "2025-01-02T15:04:05")
    val createdAt: LocalDateTime,
    @Schema(description = "최종 수정 시각(UTC)", example = "2025-01-03T10:00:00")
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                loginId = user.loginId,
                name = user.name,
                isStudio = user.isStudio,
                birthDate = user.birthDate,
                sex = user.sex,
                addInfoIsSkip = user.addInfoIsSkip,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            )
        }
    }
}
