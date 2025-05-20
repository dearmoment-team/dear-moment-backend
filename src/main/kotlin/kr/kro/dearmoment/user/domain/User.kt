package kr.kro.dearmoment.user.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * 유저 도메인 모델
 * - id: DB에서 생성되는 PK
 * - loginId: 사용자 로그인용 ID
 * - password: 비밀번호
 * - name: 이름
 * - isStudio: 스튜디오 유저 여부
 * - createdAt, updatedAt: 생성/수정 시각
 * - kakaoId: 카카오 로그인 고유식별 id
 * - birthDate: 생년월일
 * - sex: 성별
 */
data class User(
    val id: UUID? = null,
    val loginId: String? = null,
    val password: String? = null,
    val name: String,
    val isStudio: Boolean? = false,
    // 추후 null 불가
    val kakaoId: Long? = null,
    val birthDate: LocalDate? = null,
    val sex: Sex? = null,
    val addInfoIsSkip: Boolean? = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        // 이메일 가입 유저인 경우, loginId/password가 필수
        if (kakaoId == null) {
            require(!loginId.isNullOrBlank()) { "로그인 아이디(loginId)는 비어있을 수 없습니다. (이메일 가입)" }
            require(!password.isNullOrBlank()) { "비밀번호(password)는 비어있을 수 없습니다. (이메일 가입)" }
        }
        require(name.isNotBlank()) { "이름(name)은 비어있을 수 없습니다." }

        // createdAt < updatedAt 검증
        if (updatedAt != null) {
            require(!createdAt.isAfter(updatedAt)) {
                "createdAt이 updatedAt 보다 이후일 수 없습니다."
            }
        }
    }

    fun updateProfile(
        newName: String?,
        newIsStudio: Boolean?,
        newBirthDate: LocalDate?,
        newSex: Sex?,
        now: LocalDateTime,
        newAddInfoIsSkip: Boolean? = null,
    ): User {
        require(!createdAt.isAfter(now)) { "수정 시점이 생성 시점보다 이를 수 없습니다." }

        // ① 업데이트 후 최종적으로 들어갈 birthDate/sex 계산
        val finalBirthDate = newBirthDate ?: this.birthDate
        val finalSex = newSex ?: this.sex
        val calculatedAddInfoIsSkip: Boolean =
            if (newAddInfoIsSkip == true) {
                true
            } else {
                // ② 규칙: 두 값 중 모두 존재하면 true
                (finalBirthDate != null && finalSex != null)
            }

        return this.copy(
            name = newName ?: this.name,
            isStudio = newIsStudio ?: this.isStudio,
            birthDate = finalBirthDate,
            sex = finalSex,
            updatedAt = now,
            // ③ newAddInfoIsSkip 파라미터 무시, 계산된 값 사용
            addInfoIsSkip = calculatedAddInfoIsSkip
        )
    }
}
