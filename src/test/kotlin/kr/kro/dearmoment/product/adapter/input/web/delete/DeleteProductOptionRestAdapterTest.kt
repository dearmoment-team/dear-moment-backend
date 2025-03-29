package kr.kro.dearmoment.product.adapter.input.web.delete

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DeleteProductOptionRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 옵션 삭제 API 테스트 - 정상 삭제`() {
        // 인증된 userId와 상품 ID, 옵션 ID를 함께 전달
        every { deleteProductOptionUseCase.deleteOption(userId, 1L, 100L) } just Runs

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{productId}/options/{optionId}", 1L, 100L)
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product-option")
    }

    @Test
    fun `상품 옵션 삭제 API 테스트 - 존재하지 않는 옵션`() {
        every {
            deleteProductOptionUseCase.deleteOption(userId, 1L, 999L)
        } throws IllegalArgumentException("The option to delete does not exist: 999.")

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{productId}/options/{optionId}", 1L, 999L)
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "delete-product-option-not-found",
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }
}
