package kr.kro.dearmoment.inquiry.adapter.output.persistence.author

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorInquiryJpaRepository : JpaRepository<AuthorInquiryEntity, Long> {
    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<AuthorInquiryEntity>
}
