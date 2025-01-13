package kr.kro.onboarding.boardgame.adapter.input.web

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.kro.onboarding.common.dto.BaseResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(RestDocumentationExtension::class)
@WebMvcTest(BoardGameController::class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class BoardGameControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    private val restDocumentation: RestDocumentationResultHandler =
        MockMvcRestDocumentationWrapper
            .document(
                "{class-name}/{method-name}",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
            )

    @Test
    fun healthCheck() {
        val expectedResponse = BaseResponse(success = true, code = 200, data = "OK")

        mockMvc.perform(get("/health"))
            .andExpect { status().isOk }
            .andExpect { content().json(objectMapper.writeValueAsString(expectedResponse)) }
            .andDo(restDocumentation)
    }
}
