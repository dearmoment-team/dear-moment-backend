package kr.kro.dearmoment.inquiry.adapter.output.persistence.studio

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface StudioInquiryJpaRepository : JpaRepository<StudioInquiryEntity, Long>, KotlinJdslJpqlExecutor
