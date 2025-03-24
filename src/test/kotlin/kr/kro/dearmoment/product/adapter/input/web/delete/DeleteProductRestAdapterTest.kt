package kr.kro.dearmoment.product.adapter.input.web.delete

import andDocument
import kr.kro.dearmoment.common.MockBaseApiTest
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapter
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ProductRestAdapter::class)
class DeleteProductRestAdapterTest : MockBaseApiTest() {
    @Test
    fun `상품 삭제 API 테스트 - 정상 삭제`() {
        // given
        doNothing().`when`(deleteProductUseCase).deleteProduct(1L)

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product")
    }

    @Test
    fun `상품 삭제 API 테스트 - 존재하지 않는 상품`() {
        // given
        doThrow(IllegalArgumentException("The product to delete does not exist: 999."))
            .`when`(deleteProductUseCase).deleteProduct(999L)

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)

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
