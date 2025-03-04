package kr.kro.dearmoment.inquiry.adapter.input.web.product

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.DATETIME
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.means
import kr.kro.dearmoment.common.restdocs.pathParameters
import kr.kro.dearmoment.common.restdocs.queryParameters
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateProductInquiryRequest
import kr.kro.dearmoment.inquiry.application.dto.GetProductInquiryResponse
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class ProductInquiryRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 문의 생성 API`() {
        val requestBody =
            CreateProductInquiryRequest(
                userId = 123L,
                productId = 11L,
            )

        val expected = CreateInquiryResponse(1L)

        every { createInquiryUseCase.createProductInquiry(any()) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .post("/api/inquiries/products")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-product_inquiry",
                requestBody(
                    "userId" type NUMBER means "유저 ID",
                    "productId" type NUMBER means "상품 ID",
                ),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.inquiryId" type NUMBER means "상품 문의 ID",
                    "success" type BOOLEAN means "성공여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }

    @Test
    fun `상품 문의 조회 API`() {
        val userId = 123L
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))

        val inquiries =
            listOf(
                GetProductInquiryResponse(
                    inquiryId = 1L,
                    productId = 2L,
                    thumbnailUrl = "https://storage.com/photo/product1",
                    createdDate = LocalDateTime.now(),
                ),
                GetProductInquiryResponse(
                    inquiryId = 2L,
                    productId = 3L,
                    thumbnailUrl = "https://storage.com/photo/product2",
                    createdDate = LocalDateTime.now(),
                ),
            )

        val page = PageImpl(inquiries, pageable, inquiries.size.toLong())

        val expectedResponse =
            PagedResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
            )

        every { getInquiryUseCase.getProductInquiries(GetProductInquiresQuery(userId, pageable)) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/inquiries/products/{userId}", userId)
                .queryParam("page", "0")
                .queryParam("size", "10")

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-product_inquiries",
                pathParameters("userId" means "상품 문의를 생성한 userId"),
                queryParameters(
                    "page" means "조회할 페이지 번호 (0부터 시작)",
                    "size" means "페이지 크기 (기본값: 10)",
                ),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.content" type ARRAY means "상품 문의 리스트",
                    "data.content[].inquiryId" type NUMBER means "상품 문의 ID",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].thumbnailUrl" type STRING means "대표 이미지 URL",
                    "data.content[].createdDate" type DATETIME means "문의 생성 날짜",
                    "data.totalPages" type NUMBER means "전체 페이지 수",
                    "data.totalElements" type NUMBER means "전체 데이터 개수",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }

    @Test
    fun `상품 문의 삭제 API`() {
        val inquiryId = 1L

        every { removeInquiryUseCase.removeProductInquiry(inquiryId) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/inquiries/products/{inquiryId}", inquiryId)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-product-inquiry",
                pathParameters(
                    "inquiryId" means "삭제할 상품 문의 ID",
                ),
            )
    }
}
