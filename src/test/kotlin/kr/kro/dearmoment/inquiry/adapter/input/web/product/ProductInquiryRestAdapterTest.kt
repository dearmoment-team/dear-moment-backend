package kr.kro.dearmoment.inquiry.adapter.input.web.product

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.DATETIME
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.means
import kr.kro.dearmoment.common.restdocs.pathParameters
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.CreateProductInquiryRequest
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiriesResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiryResponse
import org.junit.jupiter.api.Test
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

        val expected =
            GetProductInquiriesResponse(
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
                ),
            )

        every { getInquiryUseCase.getProductInquiries(userId) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .get("/api/inquiries/products/{userId}", userId)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-product_inquiries",
                pathParameters("userId" means "상품 문의를 생성한 userId"),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.inquiries" type ARRAY means "상품 문의 리스트",
                    "data.inquiries[].inquiryId" type NUMBER means "상품 문의 ID",
                    "data.inquiries[].productId" type NUMBER means "상품 ID",
                    "data.inquiries[].thumbnailUrl" type STRING means "대표 이미지 URL",
                    "data.inquiries[].createdDate" type DATETIME means "문의 생성 날짜",
                    "success" type BOOLEAN means "성공여부",
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
