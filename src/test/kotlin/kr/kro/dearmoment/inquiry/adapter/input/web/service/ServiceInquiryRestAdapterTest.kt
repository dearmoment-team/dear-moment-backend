package kr.kro.dearmoment.inquiry.adapter.input.web.service

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.BOOLEAN
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
import kr.kro.dearmoment.inquiry.adapter.input.web.service.dto.CreateServiceInquiryRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ServiceInquiryRestAdapterTest : RestApiTestBase() {
    @Test
    fun `서비스 문의 생성 API`() {
        val requestBody =
            CreateServiceInquiryRequest(
                userId = 123L,
                type = "SERVICE_COMPLIMENT",
                content = "서비스가 너무 편합니다.",
            )

        val expected = CreateInquiryResponse(1L)

        every { createInquiryUseCase.createServiceInquiry(any()) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .post("/api/inquiries/services")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-service_inquiry",
                requestBody(
                    "userId" type NUMBER means "유저 ID",
                    "type" type STRING means "서비스 문의 타입 " +
                        "(\"SERVICE_COMPLIMENT\"," +
                        " \"SERVICE_SUGGESTION\", " +
                        "\"SYSTEM_IMPROVEMENT\", " +
                        "\"SYSTEM_ERROR_REPORT\")",
                    "content" type STRING means "내용",
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
    fun `서비스 문의 삭제 API`() {
        val inquiryId = 1L

        every { removeInquiryUseCase.removeServiceInquiry(inquiryId) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/inquiries/services/{inquiryId}", inquiryId)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-service-inquiry",
                pathParameters(
                    "inquiryId" means "삭제할 상품 문의 ID",
                ),
            )
    }
}
