package kr.kro.dearmoment.inquiry.adapter.output.persistence

import kr.kro.dearmoment.inquiry.adapter.output.persistence.author.AuthorInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.author.AuthorInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryJpaRepository
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class InquiryPersistenceAdapter(
    private val authorInquiryJpaRepository: AuthorInquiryJpaRepository,
    private val productInquiryJpaRepository: ProductInquiryJpaRepository,
    private val serviceInquiryJpaRepository: ServiceInquiryJpaRepository,
) : SaveInquiryPort, GetInquiryPort, DeleteInquiryPort {
    override fun saveProductInquiry(inquiry: ProductInquiry): Long {
        val entity = ProductInquiryEntity.from(inquiry)
        return productInquiryJpaRepository.save(entity).id
    }

    override fun saveAuthorInquiry(inquiry: AuthorInquiry): Long {
        val entity = AuthorInquiryEntity.from(inquiry)
        return authorInquiryJpaRepository.save(entity).id
    }

    override fun saveServiceInquiry(inquiry: ServiceInquiry): Long {
        val entity = ServiceInquiryEntity.from(inquiry)
        return serviceInquiryJpaRepository.save(entity).id
    }

    override fun getAuthorInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<AuthorInquiry> {
        val entities = authorInquiryJpaRepository.findByUserId(userId, pageable)
        return entities.map { it.toDomain() }
    }

    override fun getProductInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductInquiry> {
        val entities = productInquiryJpaRepository.findByUserId(userId, pageable)
        return entities.map { it.toDomain() }
    }

    override fun deleteProductInquiry(inquiryId: Long): Unit = productInquiryJpaRepository.deleteById(inquiryId)
}
