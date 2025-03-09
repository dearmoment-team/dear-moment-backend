package kr.kro.dearmoment.inquiry.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

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
                val savedStudio = studioRepository.save(studioEntityFixture(userId = 33333L))
                val savedProduct = productRepository.save(productEntityFixture(33333L, savedStudio))
                val option = productOptionRepository.save(productOptionEntityFixture(savedProduct))
                val inquiry = CreateProductOptionInquiry(userId = 1L, productOptionId = option.optionId!!)
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveProductOptionInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("saveStudioInquiry()는") {
            context("StudioInquiry 가 전달되면") {
                val inquiry = StudioInquiry(userId = 1L, title = "작가의 상풍 정보 문의", content = "작가 상풍 정보가 잘못되었습니다.")
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveStudioInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("saveServiceInquiry()는") {
            context("serviceInquiry 가 전달되면") {
                val inquiry =
                    ServiceInquiry(userId = 1L, type = ServiceInquiryType.SYSTEM_ERROR_REPORT, content = "디어모먼트 상품이 안열립니다.")
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveServiceInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("findUserStudioInquiries()는") {
            val userId = 1L
            val inquiries =
                listOf(
                    StudioInquiry(
                        userId = userId,
                        title = "문의1 제목",
                        content = "문의1 내용",
                    ),
                    StudioInquiry(
                        userId = userId,
                        title = "문의2 제목",
                        content = "문의2 내용",
                    ),
                )
            inquiries.forEach { adapter.saveStudioInquiry(it) }

            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))

            context("userId와 Pageable이 전달되면") {
                it("DB에서 페이징된 스튜디오 문의 목록을 반환한다.") {
                    val result = adapter.findUserStudioInquiries(userId, pageable)

                    result.totalElements shouldBe inquiries.size.toLong()
                    result.content.size shouldBe inquiries.size
                    result.totalPages shouldBe 1
                    result.number shouldBe 0
                    result.size shouldBe 10
                }
            }
        }

        describe("findUserProductOptionInquiries()는") {
            val userId = 1L
            val studio = studioRepository.save(studioEntityFixture())
            val product = productRepository.save(productEntityFixture(studioEntity = studio))
            val options = productOptionRepository.saveAll(List(3) { productOptionEntityFixture(product) })
            val inquiries =
                options.map {
                    CreateProductOptionInquiry(
                        userId = userId,
                        productOptionId = it.optionId!!,
                    )
                }

            inquiries.forEach { adapter.saveProductOptionInquiry(it) }
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            context("userId가 전달되면") {
                it("DB 에서 유저가 작성한 모든 상품 문의를 반환한다.") {
                    val result = adapter.findUserProductOptionInquiries(userId, pageable)

                    result.totalElements shouldBe inquiries.size.toLong()
                    result.content.size shouldBe inquiries.size
                    result.totalPages shouldBe 1
                    result.number shouldBe 0
                    result.size shouldBe 10
                }
            }
        }

        describe("deleteProductOptionInquiry()는") {
            context("inquiryId가 전될되면") {
                it("DB의 종류별 문의 테이블에서 해당 PK를 가진 데이터를 삭제한다.") {
                    shouldNotThrow<Throwable> { adapter.deleteProductOptionInquiry(1L) }
                }
            }
        }
    })
