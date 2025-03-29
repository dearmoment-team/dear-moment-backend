package kr.kro.dearmoment.inquiry.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.fixture.productOptionInquiryFixture
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
import kr.kro.dearmoment.inquiry.application.query.GetStudioInquiresQuery
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID

class InquiryQueryServiceTest : DescribeSpec({
    val getPort = mockk<GetInquiryPort>()
    val service = InquiryQueryService(getPort)

    describe("getStudioInquiries()는") {
        context("userId와 Pageable이 전달되면") {
            val userId = UUID.randomUUID()
            val inquiries =
                listOf(
                    StudioInquiry(
                        id = 1L,
                        userId = userId,
                        title = "문의1 제목",
                        content = "문의1 내용",
                    ),
                    StudioInquiry(
                        id = 2L,
                        userId = userId,
                        title = "문의2 제목",
                        content = "문의2 내용",
                    ),
                )

            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            val page: Page<StudioInquiry> = PageImpl(inquiries, pageable, inquiries.size.toLong())

            every { getPort.findUserStudioInquiries(userId, pageable) } returns page

            it("유저의 스튜디오 문의를 페이징하여 반환한다.") {
                val result = service.getStudioInquiries(GetStudioInquiresQuery(userId, pageable))

                result.totalElements shouldBe inquiries.size.toLong()
                result.content.size shouldBe inquiries.size
                result.totalPages shouldBe 1
                result.page shouldBe 0
                result.size shouldBe 10

                verify(exactly = 1) { getPort.findUserStudioInquiries(userId, pageable) }
            }
        }
    }

    describe("getProductOptionInquiries()는") {
        context("userId가 전달되면") {
            val userId = UUID.randomUUID()
            val inquiries = List(3) { productOptionInquiryFixture(userId) }

            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            val page: Page<ProductOptionInquiry> = PageImpl(inquiries, pageable, inquiries.size.toLong())

            every { getPort.findUserProductOptionInquiries(userId, pageable) } returns page

            it("유저의 상품 옵션 문의를 모두 반환한다.") {
                val result = service.getProductOptionInquiries(GetProductInquiresQuery(userId, pageable))

                result.totalElements shouldBe inquiries.size.toLong()
                result.content.size shouldBe inquiries.size
                result.totalPages shouldBe 1
                result.page shouldBe 0
                result.size shouldBe 10

                verify(exactly = 1) { getPort.findUserProductOptionInquiries(userId, pageable) }
            }
        }
    }
})
