package kr.kro.dearmoment.inquiry.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryJpaRepository
import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import java.util.UUID

@RepositoryTest
class InquiryPersistenceAdapterTest(
    private val studioInquiryJpaRepository: StudioInquiryJpaRepository,
    private val productInquiryJpaRepository: ProductOptionInquiryJpaRepository,
    private val serviceInquiryJpaRepository: ServiceInquiryJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val productRepository: JpaProductRepository,
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        val adapter =
            InquiryPersistenceAdapter(
                studioInquiryJpaRepository,
                productInquiryJpaRepository,
                serviceInquiryJpaRepository,
                productOptionRepository,
            )

        describe("saveProductOptionInquiry()는") {
            context("CreateProductOptionInquiry 가 전달되면") {
                val savedStudio = studioRepository.save(studioEntityFixture())
                val savedProduct = productRepository.save(productEntityFixture(savedStudio.userId, savedStudio))
                val option = productOptionRepository.save(productOptionEntityFixture(savedProduct))
                val inquiry =
                    CreateProductOptionInquiry(
                        userId = UUID.randomUUID(),
                        productId = savedProduct.productId!!,
                        optionId = option.optionId,
                    )
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveProductOptionInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("saveStudioInquiry()는") {
            context("StudioInquiry 가 전달되면") {
                val inquiry = StudioInquiry(userId = UUID.randomUUID(), title = "작가의 상풍 정보 문의", content = "작가 상풍 정보가 잘못되었습니다.")
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveStudioInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("saveServiceInquiry()는") {
            context("serviceInquiry 가 전달되면") {
                val inquiry =
                    ServiceInquiry(userId = UUID.randomUUID(), type = ServiceInquiryType.SYSTEM_ERROR_REPORT, content = "디어모먼트 상품이 안열립니다.")
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveServiceInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("deleteProductOptionInquiry()는") {
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(savedStudio.userId, savedStudio))
            val option = productOptionRepository.save(productOptionEntityFixture(savedProduct))
            val inquiry =
                CreateProductOptionInquiry(
                    userId = UUID.randomUUID(),
                    productId = savedProduct.productId!!,
                    optionId = option.optionId,
                )
            val savedInquiry = adapter.saveProductOptionInquiry(inquiry)
            context("userId와 inquiryId가 전될되면") {
                it("DB의 종류별 문의 테이블에서 해당 PK를 가진 데이터를 삭제한다.") {
                    shouldNotThrow<Throwable> { adapter.deleteProductOptionInquiry(savedInquiry, inquiry.userId) }
                }

                it("존재하지 않는 유저의 문의를 삭제하려면 예외를 발생 시킨다") {
                    shouldThrow<CustomException> { adapter.deleteProductOptionInquiry(savedInquiry, UUID.randomUUID()) }
                }
            }
        }
    })
