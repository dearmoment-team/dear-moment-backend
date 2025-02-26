package kr.kro.dearmoment.inquiry.adapter.input.web.author

import andDocument
import io.mockk.every
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
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.CreateAuthorInquiryRequest
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiriesResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AuthorInquiryRestAdapterTest : RestApiTestBase() {
    @Test
    fun `작가 문의 생성 API`() {
        val requestBody =
            CreateAuthorInquiryRequest(
                userId = 123L,
                title = "작가 정보 오류 문의합니다.",
                content = "연락처가 잘못 된 것 같습니다.",
                email = "email@email.com",
            )

        val expected = CreateInquiryResponse(1L)

        every { createInquiryUseCase.createAuthorInquiry(any()) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .post("/api/inquiries/authors")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-author_inquiry",
                requestBody(
                    "userId" type NUMBER means "유저 ID",
                    "title" type STRING means "문의 제목",
                    "content" type STRING means "문의 내용",
                    "email" type STRING means "답변 받을 이메일",
                ),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.inquiryId" type NUMBER means "문의 ID",
                    "success" type BOOLEAN means "성공여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }

    @Test
    fun `작가 문의 조회 API`() {
        val userId = 123L

        val expected =
            GetAuthorInquiriesResponse(
                listOf(
                    GetAuthorInquiryResponse(
                        inquiryId = 1L,
                        title = "문의1 제목",
                        content = "문의1 내용",
                        createdDate = LocalDateTime.now(),
                    ),
                    GetAuthorInquiryResponse(
                        inquiryId = 2L,
                        title = "문의2 제목",
                        content = "문의2 내용",
                        createdDate = LocalDateTime.now(),
                    ),
                ),
            )

        every { getInquiryUseCase.getAuthorInquiries(userId) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .get("/api/inquiries/authors/{userId}", userId)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-author_inquiries",
                pathParameters("userId" means "작가 문의를 생성한 userId"),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.inquiries" type ARRAY means "작가 문의 리스트",
                    "data.inquiries[].inquiryId" type NUMBER means "작가 문의 ID",
                    "data.inquiries[].title" type STRING means "제목",
                    "data.inquiries[].content" type STRING means "내용",
                    "data.inquiries[].answer" type STRING means "답변 내용",
                    "data.inquiries[].createdDate" type DATETIME means "문의 생성 날짜",
                    "success" type BOOLEAN means "성공여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }
}
