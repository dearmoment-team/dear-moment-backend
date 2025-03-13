package kr.kro.dearmoment.inquiry.adapter.output.persistence.product

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface ProductOptionInquiryJpaRepository : JpaRepository<ProductOptionInquiryEntity, Long>, KotlinJdslJpqlExecutor
