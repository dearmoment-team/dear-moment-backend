package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductOptionLikeJpaRepository : JpaRepository<ProductOptionLikeEntity, Long>, KotlinJdslJpqlExecutor {
    fun findByIdAndUserId(
        likeId: Long,
        userId: UUID,
    ): ProductOptionLikeEntity?

    fun findByUserId(userId: UUID): List<ProductOptionLikeEntity>
}
