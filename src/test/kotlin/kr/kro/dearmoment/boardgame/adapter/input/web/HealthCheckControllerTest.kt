package kr.kro.dearmoment.boardgame.adapter.input.web

import andDocument
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.BOOLEAN
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.NUMBER
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.OBJECT
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.STRING
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.responseBody
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.toJson
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.type
import kr.kro.dearmoment.common.dto.BaseResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(RestDocumentationExtension::class)
@WebMvcTest(HealthCheckController::class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class HealthCheckControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun healthCheck() {
        val expectedResponse = BaseResponse.success(data = HealthCheckResponse(value = "OK"))
        val request = RestDocumentationRequestBuilders.request(HttpMethod.GET, "/health")

        mockMvc.perform(request)
            .andExpect { status().isOk }
            .andExpect { expectedResponse.toJson() }
            .andDocument(
                "health-check-url",
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.value" type STRING means "데이터 값",
                    "success" type BOOLEAN means "성공여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }
}
