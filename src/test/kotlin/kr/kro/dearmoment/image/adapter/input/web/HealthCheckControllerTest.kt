package kr.kro.dearmoment.image.adapter.input.web

import andDocument
import kr.kro.dearmoment.image.adapter.input.web.restdocs.BOOLEAN
import kr.kro.dearmoment.image.adapter.input.web.restdocs.NUMBER
import kr.kro.dearmoment.image.adapter.input.web.restdocs.OBJECT
import kr.kro.dearmoment.image.adapter.input.web.restdocs.STRING
import kr.kro.dearmoment.image.adapter.input.web.restdocs.responseBody
import kr.kro.dearmoment.image.adapter.input.web.restdocs.toJson
import kr.kro.dearmoment.image.adapter.input.web.restdocs.type
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class HealthCheckControllerTest : RestApiTestBase() {
    @Test
    fun healthCheck() {
        val expectedResponse = HealthCheckResponse(value = "OK")
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
