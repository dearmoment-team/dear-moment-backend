package kr.kro.dearmoment.inquiry.adapter.output.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.common.fixture.studioInquiryEntityFixture
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryJpaRepository
import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID

@RepositoryTest
class InquiryReadOnlyPersistenceAdapterTest(
    private val studioInquiryJpaRepository: StudioInquiryJpaRepository,
    private val productOptionInquiryJpaRepository: ProductOptionInquiryJpaRepository,
    private val serviceInquiryJpaRepository: ServiceInquiryJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val productRepository: JpaProductRepository,
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        val adapter = InquiryReadOnlyPersistenceAdapter(studioInquiryJpaRepository, productOptionInquiryJpaRepository)

        val adapter2 =
            InquiryPersistenceAdapter(
                studioInquiryJpaRepository,
                productOptionInquiryJpaRepository,
                serviceInquiryJpaRepository,
                productOptionRepository,
            )

        afterTest {
            productOptionInquiryJpaRepository.deleteAll()
            productOptionRepository.deleteAll()
            productRepository.deleteAll()
            studioRepository.deleteAll()
        }

        describe("findUserStudioInquiries()는") {
            val userId = UUID.randomUUID()
            val studioInquiries = List(3) { studioInquiryEntityFixture(userId) }
            studioInquiries.forEach { studioInquiryJpaRepository.save(it) }

            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))

            context("userId와 Pageable이 전달되면") {
                it("DB에서 페이징된 스튜디오 문의 목록을 반환한다.") {
                    val result = adapter.findUserStudioInquiries(userId, pageable)

                    result.totalElements shouldBe studioInquiries.size.toLong()
                    result.content.size shouldBe studioInquiries.size
                    result.totalPages shouldBe (studioInquiries.size / pageable.pageSize) +
                        if (studioInquiries.size % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("findUserProductOptionInquiries()는") {
            val userId = UUID.randomUUID()
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))
            val inquiries =
                listOf(
                    CreateProductOptionInquiry(
                        userId = userId,
                        productId = savedProduct.productId!!,
                        optionId = savedProductOption.optionId,
                    ),
                )

            adapter2.saveProductOptionInquiry(inquiries[0])

            context("userId가 전달되면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB 에서 유저가 작성한 모든 상품 문의를 반환한다.") {
                    val result = adapter.findUserProductOptionInquiries(userId, pageable)

                    result.totalElements shouldBe inquiries.size.toLong()
                    result.content.size shouldBe inquiries.size
                    result.totalPages shouldBe (inquiries.size / pageable.pageSize) +
                        if (inquiries.size % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }
    })
