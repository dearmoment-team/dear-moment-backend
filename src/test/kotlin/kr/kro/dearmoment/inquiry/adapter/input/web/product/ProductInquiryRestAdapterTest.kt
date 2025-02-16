package kr.kro.dearmoment.inquiry.adapter.input.web.product

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.means
import kr.kro.dearmoment.common.restdocs.pathParameters
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.CreateProductInquiryRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
