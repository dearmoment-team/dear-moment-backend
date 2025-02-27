package kr.kro.dearmoment.inquiry.adapter.output.persistence.service

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceInquiryJpaRepository : JpaRepository<ServiceInquiryEntity, Long>
