package kr.kro.dearmoment.inquiry.adapter.input.web.artist

import andDocument
import io.mockk.every
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
import kr.kro.dearmoment.inquiry.application.dto.CreateArtistInquiryRequest
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetArtistInquiryResponse
import kr.kro.dearmoment.inquiry.application.query.GetArtistInquiresQuery
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class ArtistInquiryRestAdapterTest : RestApiTestBase() {
    @Test
    fun `작가 문의 생성 API`() {
        val requestBody =
            CreateArtistInquiryRequest(
                userId = 123L,
                title = "작가 정보 오류 문의합니다.",
                content = "연락처가 잘못 된 것 같습니다.",
                email = "email@email.com",
            )

        val expected = CreateInquiryResponse(1L)

        every { createInquiryUseCase.createArtistInquiry(any()) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .post("/api/inquiries/artists")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-artist_inquiry",
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
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
        val inquiries =
            listOf(
                GetArtistInquiryResponse(
                    inquiryId = 1L,
                    title = "문의1 제목",
                    content = "문의1 내용",
                    createdDate = LocalDateTime.now(),
                ),
                GetArtistInquiryResponse(
                    inquiryId = 2L,
                    title = "문의2 제목",
                    content = "문의2 내용",
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

        every { getInquiryUseCase.getArtistInquiries(GetArtistInquiresQuery(userId, pageable)) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/inquiries/artists/{userId}", userId)
                .queryParam("page", "0")
                .queryParam("size", "10")

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.page").value(expectedResponse.page))
            .andExpect(jsonPath("$.data.size").value(expectedResponse.size))
            .andExpect(jsonPath("$.data.totalElements").value(expectedResponse.totalElements))
            .andExpect(jsonPath("$.data.totalPages").value(expectedResponse.totalPages))
            .andDocument(
                "get-artist_inquiries",
                pathParameters("userId" means "작가 문의를 생성한 userId"),
                queryParameters(
                    "page" means "조회할 페이지 번호 (0부터 시작)",
                    "size" means "페이지 크기 (기본값: 10)",
                ),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.content" type ARRAY means "작가 문의 리스트",
                    "data.content[].inquiryId" type NUMBER means "작가 문의 ID",
                    "data.content[].title" type STRING means "제목",
                    "data.content[].content" type STRING means "내용",
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
}
