package kr.kro.dearmoment.like.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaLikeRepository : JpaRepository<LikeEntity, Long> {
    fun existsByUserIdAndTargetIdAndType(
        userId: Long,
        targetId: Long,
        type: String,
    ): Boolean

    fun findByUserId(userId: Long): List<LikeEntity>
}
