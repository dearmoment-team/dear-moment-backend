package kr.kro.dearmoment.inquiry.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.inquiry.adapter.output.persistence.author.AuthorInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductInquiryJpaRepository
import kr.kro.dearmoment.inquiry.adapter.output.persistence.service.ServiceInquiryJpaRepository
import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@RepositoryTest
class InquiryPersistenceAdapterTest(
    private val authorInquiryJpaRepository: AuthorInquiryJpaRepository,
    private val productInquiryJpaRepository: ProductInquiryJpaRepository,
    private val serviceInquiryJpaRepository: ServiceInquiryJpaRepository,
) : DescribeSpec({
        val adapter =
            InquiryPersistenceAdapter(authorInquiryJpaRepository, productInquiryJpaRepository, serviceInquiryJpaRepository)

        describe("saveProductInquiry()는") {
            context("productInquiry 가 전달되면") {
                val inquiry = ProductInquiry(userId = 1L, productId = 1L, thumbnailUrl = "")
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveProductInquiry(inquiry)
                    resultId shouldNotBe 0L
                }
            }
        }

        describe("saveAuthorInquiry()는") {
            context("authorInquiry 가 전달되면") {
                val inquiry = AuthorInquiry(userId = 1L, title = "작가의 상풍 정보 문의", content = "작가 상풍 정보가 잘못되었습니다.")
                it("엔티티로 변환하여 DB에 저장한다.") {
                    val resultId = adapter.saveAuthorInquiry(inquiry)
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

        describe("getAuthorInquiries()는") {
            val userId = 1L
            val inquiries =
                listOf(
                    AuthorInquiry(
                        userId = userId,
                        title = "문의1 제목",
                        content = "문의1 내용",
                    ),
                    AuthorInquiry(
                        userId = userId,
                        title = "문의2 제목",
                        content = "문의2 내용",
                    ),
                )
            inquiries.forEach { adapter.saveAuthorInquiry(it) }

            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))

            context("userId와 Pageable이 전달되면") {
                it("DB에서 페이징된 작가 문의 목록을 반환한다.") {
                    val result = adapter.getAuthorInquiries(userId, pageable)

                    result.totalElements shouldBe inquiries.size.toLong()
                    result.content.size shouldBe inquiries.size
                    result.totalPages shouldBe 1
                    result.number shouldBe 0
                    result.size shouldBe 10
                }
            }
        }

        describe("getProductInquiries()는") {
            val userId = 1L
            val inquiries =
                listOf(
                    ProductInquiry(
                        userId = userId,
                        productId = 1L,
                        thumbnailUrl = "url1",
                    ),
                    ProductInquiry(
                        userId = userId,
                        productId = 2L,
                        thumbnailUrl = "url2",
                    ),
                )
            inquiries.forEach { adapter.saveProductInquiry(it) }
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            context("userId가 전달되면") {
                it("DB 에서 유저가 작성한 모든 상품 문의를 반환한다.") {
                    val result = adapter.getProductInquiries(userId, pageable)

                    result.totalElements shouldBe inquiries.size.toLong()
                    result.content.size shouldBe inquiries.size
                    result.totalPages shouldBe 1
                    result.number shouldBe 0
                    result.size shouldBe 10
                }
            }
        }

        describe("deleteProductInquiry()는") {
            context("inquiryId가 전될되면") {
                it("DB의 종류별 문의 테이블에서 해당 PK를 가진 데이터를 삭제한다.") {
                    shouldNotThrow<Throwable> { adapter.deleteProductInquiry(1L) }
                }
            }
        }
    })
