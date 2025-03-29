package kr.kro.dearmoment.inquiry.adapter.input.web

import andDocument
import io.mockk.every
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateServiceInquiryRequest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ServiceInquiryRestAdapterTest : RestApiTestBase() {
    @Test
    fun `서비스 문의 생성 API`() {
        val requestBody =
            CreateServiceInquiryRequest(
                type = "SERVICE_COMPLIMENT",
                content = "서비스가 너무 편합니다.",
                email = "email@email.com",
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
                    "email" type STRING means "답변 받을 이메일",
                ),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.inquiryId" type NUMBER means "상품 문의 ID",
                    "success" type BOOLEAN means "성공여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }
}
