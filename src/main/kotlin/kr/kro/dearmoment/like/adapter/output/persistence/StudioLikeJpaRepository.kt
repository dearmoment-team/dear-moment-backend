package kr.kro.dearmoment.like.adapter.output.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface StudioLikeJpaRepository : JpaRepository<StudioLikeEntity, Long> {
    fun existsByUserIdAndStudioId(
        userId: Long,
        studioId: Long,
    ): Boolean

    @Query(
        """
        SELECT sl FROM StudioLikeEntity sl
        JOIN FETCH sl.studio s
        WHERE sl.userId = :userId
        """,
    )
    fun getUserLikes(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): Page<StudioLikeEntity>
}
