package kr.kro.dearmoment.user.domain

import java.time.LocalDateTime

/**
 * 유저 도메인 모델
 * - id: DB에서 생성되는 PK
 * - loginId: 사용자 로그인용 ID
 * - password: 비밀번호
 * - name: 이름
 * - isStudio: 스튜디오 유저 여부
 * - createdAt, updatedAt: 생성/수정 시각
 * - createdUser, updatedUser: 생성/수정 유저
 */
data class User(
    val id: Long = 0L,
    val loginId: String,
    val password: String,
    val name: String,
    val isStudio: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdUser: String,
    val updatedUser: String
) {
    init {
        require(loginId.isNotBlank()) { "로그인 아이디는 비어있을 수 없습니다." }
        require(password.isNotBlank()) { "비밀번호는 비어있을 수 없습니다." }
        require(name.isNotBlank()) { "이름은 비어있을 수 없습니다." }
        require(!createdAt.isAfter(updatedAt)) {
            "createdAt은 updatedAt보다 이후일 수 없습니다."
        }
    }
}
