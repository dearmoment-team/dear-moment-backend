package kr.kro.dearmoment.inquiry.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryJpaRepository
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class InquiryPersistenceAdapter(
    private val studioInquiryJpaRepository: StudioInquiryJpaRepository,
    private val productOptionInquiryJpaRepository: ProductOptionInquiryJpaRepository,
    private val serviceInquiryJpaRepository: ServiceInquiryJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
) : SaveInquiryPort, GetInquiryPort, DeleteInquiryPort {
    override fun saveProductOptionInquiry(inquiry: CreateProductOptionInquiry): Long {
        val optionEntity =
            productOptionRepository.findByIdOrNull(inquiry.productOptionId)
                ?: throw CustomException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)
        val entity = ProductOptionInquiryEntity.from(inquiry, optionEntity)

        return productOptionInquiryJpaRepository.save(entity).id
    }

    override fun saveStudioInquiry(inquiry: StudioInquiry): Long {
        val entity = StudioInquiryEntity.from(inquiry)
        return studioInquiryJpaRepository.save(entity).id
    }

    override fun saveServiceInquiry(inquiry: ServiceInquiry): Long {
        val entity = ServiceInquiryEntity.from(inquiry)
        return serviceInquiryJpaRepository.save(entity).id
    }

    override fun findUserStudioInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<StudioInquiry> {
        val entities = studioInquiryJpaRepository.findByUserId(userId, pageable)
        return entities.map { it.toDomain() }
    }

    override fun findUserProductOptionInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductOptionInquiry> {
        val entities = productOptionInquiryJpaRepository.findByUserId(userId, pageable)
        return entities.map { it.toDomain() }
    }

    override fun deleteProductOptionInquiry(inquiryId: Long): Unit = productOptionInquiryJpaRepository.deleteById(inquiryId)
}
