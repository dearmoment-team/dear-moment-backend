package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductLikeJpaRepository : JpaRepository<ProductLikeEntity, Long>, KotlinJdslJpqlExecutor {
    fun findByIdAndUserId(
        likeId: Long,
        userId: UUID,
    ): ProductLikeEntity?
}
