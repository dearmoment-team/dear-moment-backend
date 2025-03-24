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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DeleteProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 삭제 API 테스트 - 정상 삭제`() {
        // given
        every { (deleteProductUseCase).deleteProduct(1L) } just Runs

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product")
    }

    @Test
    fun `상품 삭제 API 테스트 - 존재하지 않는 상품`() {
        // given
        every { deleteProductUseCase.deleteProduct(999L) } throws IllegalArgumentException("The product to delete does not exist: 999.")

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())

        // then
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
