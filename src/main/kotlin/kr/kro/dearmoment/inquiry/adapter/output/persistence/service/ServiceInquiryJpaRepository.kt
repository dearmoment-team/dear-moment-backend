package kr.kro.dearmoment.inquiry.adapter.output.persistence.service

import org.springframework.data.jpa.repository.JpaRepository

interface ServiceInquiryJpaRepository : JpaRepository<ServiceInquiryEntity, Long>
