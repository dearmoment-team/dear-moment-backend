package kr.kro.dearmoment.user.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * 유저 도메인 모델
 * - id: DB 에서 생성되는 PK
 * - loginId: 사용자 로그인용 ID
 * - password: 비밀번호
 * - name: 이름
 * - isStudio: 스튜디오 유저 여부
 * - createdAt, updatedAt: 생성/수정 시각
 * - kakaoId: 카카오 로그인 고유식별 id
 * - birthDate: 생년월일
 * - sex: 성별
 * - addInfoIsSkip: 추가 입력 정보 스킵 유무
 * - addInfoIsAgree: 추가 입력 정보 수집 동의 유무
 */
data class User(
    val id: UUID? = null,
    val loginId: String? = null,
    val password: String? = null,
    val name: String? = null,
    val isStudio: Boolean? = false,
    // 추후 null 불가
    val kakaoId: Long? = null,
    val birthDate: LocalDate? = null,
    val sex: Sex? = null,
    val addInfoIsSkip: Boolean? = false,
    val addInfoIsAgree: Boolean? = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        // 이메일 가입 유저인 경우, loginId/password 가 필수
        if (kakaoId == null) {
            require(!loginId.isNullOrBlank()) { "로그인 아이디(loginId)는 비어있을 수 없습니다. (이메일 가입)" }
            require(!password.isNullOrBlank()) { "비밀번호(password)는 비어있을 수 없습니다. (이메일 가입)" }
        }

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
        newAddInfoIsAgree: Boolean? = null,
    ): User {
        require(!createdAt.isAfter(now)) { "수정 시점이 생성 시점보다 이를 수 없습니다." }

        // ───── 1) 이번 호출로 확정될 ‘최종 값’ 계산 ─────
        var nextName = newName ?: this.name
        var nextBirthDate = newBirthDate ?: this.birthDate
        var nextSex = newSex ?: this.sex
        val nextIsStudio = newIsStudio ?: this.isStudio
        val nextAgree = newAddInfoIsAgree ?: this.addInfoIsAgree
        val nextSkip =
            when (newAddInfoIsSkip) {
                true -> true // 명시적 true
                false -> false // 명시적 false
                else -> (nextBirthDate != null && nextSex != null) // 자동 계산
            }

        // ───── 2) 규칙 검증 ─────
        // 2-1. 동의가 true 면 필수 정보 3종이 모두 채워져 있어야 한다
        if (nextAgree == true) {
            require(nextName != null) { "동의가 true 일 때 이름은 비어 있을 수 없습니다." }
            require(nextBirthDate != null) { "동의가 true 일 때 출생연도는 비어 있을 수 없습니다." }
            require(nextSex != null) { "동의가 true 일 때 성별은 비어 있을 수 없습니다." }
        }

        // 2-2. 동의를 false 로 변경 시 이름·성별·출생연도가 모두 빈값으로 저장
        if (newAddInfoIsAgree == false) {
            nextName = null
            nextBirthDate = null
            nextSex = null
        }

        // ───── 3) 값 적용 ─────
        return copy(
            name = nextName,
            isStudio = nextIsStudio,
            birthDate = nextBirthDate,
            sex = nextSex,
            updatedAt = now,
            addInfoIsSkip = nextSkip,
            addInfoIsAgree = nextAgree
        )
    }

    // 공통 복사 헬퍼
    private fun with(
        name: String? = this.name,
        birth: LocalDate? = this.birthDate,
        sex: Sex? = this.sex,
        agree: Boolean? = this.addInfoIsAgree,
        skip: Boolean? = this.addInfoIsSkip,
        now: LocalDateTime,
    ) = copy(
        name = name,
        birthDate = birth,
        sex = sex,
        addInfoIsAgree = agree,
        addInfoIsSkip = skip,
        updatedAt = now
    )

    // 1) 스킵 요청 ─ addInfoIsSkip = true 만 변경
    fun skipAddInfo(now: LocalDateTime): User = with(skip = true, now = now)

    // 2) 동의 + 추가정보 입력 / 거부
    fun agreeAddInfo(
        name: String?,
        birth: LocalDate?,
        sex: Sex?,
        agreeFlag: Boolean,
        now: LocalDateTime
    ): User {
        return if (agreeFlag) {
            // 동의 = true ▸ 세 값 모두 필수
            require(name != null) { "이름 필요" }
            require(birth != null) { "출생연도 필요" }
            require(sex != null) { "성별 필요" }

            with(
                name = name,
                birth = birth,
                sex = sex,
                agree = true,
                skip = true,
                now = now
            )
        } else {
            // 동의 = false ▸ 값 전부 초기화
            with(
                name = null,
                birth = null,
                sex = null,
                agree = false,
                skip = true,
                now = now
            )
        }
    }

    // 3) 철회 요청 – agree=false & 초기화
    fun withdrawAddInfo(now: LocalDateTime): User =
        agreeAddInfo(
            name = null,
            birth = null,
            sex = null,
            agreeFlag = false,
            now = now
        )
}
