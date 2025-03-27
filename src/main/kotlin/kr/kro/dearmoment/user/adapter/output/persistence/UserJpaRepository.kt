package kr.kro.dearmoment.user.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findById(id: UUID): UserEntity?

    fun findByLoginId(loginId: String): UserEntity?

    // 새로 추가 (카카오 ID로 조회)
    fun findByKakaoId(kakaoId: Long): UserEntity?
}
