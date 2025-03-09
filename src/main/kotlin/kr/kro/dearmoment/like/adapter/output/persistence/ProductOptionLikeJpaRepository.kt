package kr.kro.dearmoment.like.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductOptionLikeJpaRepository : JpaRepository<ProductOptionLikeEntity, Long> {
    fun existsByUserIdAndOptionOptionId(
        userId: Long,
        optionId: Long,
    ): Boolean

    @Query(
        """
        SELECT pol FROM ProductOptionLikeEntity pol
        JOIN FETCH pol.option o
        JOIN FETCH o.product p
        WHERE pol.userId = :userId
        """,
    )
    fun getUserLikes(
        @Param("userId") userId: Long,
    ): List<ProductOptionLikeEntity>
}
