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

class DeleteProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 삭제 API 테스트 - 정상 삭제`() {
        // 인증된 userId와 상품 ID를 함께 전달
        every { deleteProductUseCase.deleteProduct(userId, 1L) } just Runs

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 1L)
                .header("X-USER-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product")
    }

    @Test
    fun `상품 삭제 API 테스트 - 존재하지 않는 상품`() {
        every { deleteProductUseCase.deleteProduct(userId, 999L) } throws
            IllegalArgumentException("The product to delete does not exist: 999.")

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 999L)
                .header("X-USER-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "delete-product-not-found",
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }
}
