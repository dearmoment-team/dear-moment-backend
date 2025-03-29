package kr.kro.dearmoment.inquiry.adapter.output.persistence.product

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ProductOptionInquiryJpaRepository : JpaRepository<ProductOptionInquiryEntity, Long>, KotlinJdslJpqlExecutor {
    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductOptionInquiryEntity>

    @Modifying
    @Query("DELETE FROM ProductOptionInquiryEntity p WHERE p.id = :inquiryId AND p.userId = :userId")
    fun deleteByIdAndUserId(
        inquiryId: Long,
        userId: UUID,
    )
}
