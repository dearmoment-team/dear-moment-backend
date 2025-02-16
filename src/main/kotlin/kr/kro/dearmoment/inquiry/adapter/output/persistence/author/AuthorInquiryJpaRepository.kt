package kr.kro.dearmoment.inquiry.adapter.output.persistence.author

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorInquiryJpaRepository : JpaRepository<AuthorInquiryEntity, Long>
