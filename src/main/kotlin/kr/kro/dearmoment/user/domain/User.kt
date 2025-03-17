package kr.kro.dearmoment.user.domain

import java.util.UUID
import java.time.LocalDateTime

/**
 * 유저 도메인 모델
 * - id: DB에서 생성되는 PK
 * - loginId: 사용자 로그인용 ID
 * - password: 비밀번호
 * - name: 이름
 * - isStudio: 스튜디오 유저 여부
 * - createdAt, updatedAt: 생성/수정 시각
 */
data class User(
    val id: UUID? = null,
    val loginId: String,
    val password: String,
    val name: String,
    val isStudio: Boolean? = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    init {
        require(loginId.isNotBlank()) { "로그인 아이디(loginId)는 비어있을 수 없습니다." }
        require(password.isNotBlank()) { "비밀번호(password)는 비어있을 수 없습니다." }
        require(name.isNotBlank()) { "이름(name)은 비어있을 수 없습니다." }

        // createdAt < updatedAt 검증
        if (updatedAt != null) {
            require(!createdAt.isAfter(updatedAt)) {
                "createdAt이 updatedAt보다 이후일 수 없습니다."
            }
        }
    }

    /**
     * 비밀번호 검증 메서드 (실제로는 해싱 로직 필요)
     */
    fun checkPassword(rawPassword: String): Boolean {
        return this.password == rawPassword
    }

    /**
     * userName, updatedAt 등을 수정하는 예시 메서드
     */
    fun updateName(newName: String, now: LocalDateTime): User {
        require(newName.isNotBlank()) { "새로운 이름은 비어있을 수 없습니다." }
        require(!createdAt.isAfter(now)) { "수정 시점이 생성 시점보다 이를 수 없습니다." }

        return this.copy(
            name = newName,
            updatedAt = now
        )
    }
}
