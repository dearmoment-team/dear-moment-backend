package kr.kro.dearmoment.inquiry.adapter.output.persistence

import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryJpaRepository
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class InquiryReadOnlyPersistenceAdapter(
    private val studioInquiryJpaRepository: StudioInquiryJpaRepository,
    private val productOptionInquiryJpaRepository: ProductOptionInquiryJpaRepository,
) : GetInquiryPort {
    override fun findUserStudioInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<StudioInquiry> =
        studioInquiryJpaRepository.findPage(pageable) {
            select(
                entity(StudioInquiryEntity::class),
            ).from(
                entity(StudioInquiryEntity::class),
            ).where(
                path(StudioInquiryEntity::userId).eq(userId),
            )
        }.map { it?.toDomain() }

    override fun findUserProductOptionInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductOptionInquiry> =
        productOptionInquiryJpaRepository.findPage(pageable) {
            select(
                entity(ProductOptionInquiryEntity::class),
            ).from(
                entity(ProductOptionInquiryEntity::class),
                fetchJoin(ProductOptionInquiryEntity::option),
            ).where(
                path(ProductOptionInquiryEntity::userId).eq(userId),
            )
        }.map { it?.toDomain() }
}
