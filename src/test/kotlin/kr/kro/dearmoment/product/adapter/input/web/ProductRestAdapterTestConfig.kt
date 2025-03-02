package kr.kro.dearmoment.product.adapter.input.web

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProductRestAdapterTestConfig {
    @Bean
    fun createProductUseCase(): CreateProductUseCase = mock()

    @Bean
    fun updateProductUseCase(): UpdateProductUseCase = mock()

    @Bean
    fun deleteProductUseCase(): DeleteProductUseCase = mock()

    @Bean
    fun getProductUseCase(): GetProductUseCase = mock()

    @Bean
    fun productSearchUseCase(): ProductSearchUseCase = mock()

    @Bean
    fun productUseCase(
        createProductUseCase: CreateProductUseCase,
        updateProductUseCase: UpdateProductUseCase,
        deleteProductUseCase: DeleteProductUseCase,
        getProductUseCase: GetProductUseCase,
        productSearchUseCase: ProductSearchUseCase,
    ): ProductUseCase {
        return object : ProductUseCase {
            override fun saveProduct(request: CreateProductRequest): ProductResponse {
                return createProductUseCase.saveProduct(request)
            }

            override fun updateProduct(request: UpdateProductRequest): ProductResponse {
                return updateProductUseCase.updateProduct(request)
            }

            override fun deleteProduct(productId: Long) {
                deleteProductUseCase.deleteProduct(productId)
            }

            override fun getProductById(productId: Long): ProductResponse {
                return getProductUseCase.getProductById(productId)
            }

            override fun getMainPageProducts(
                page: Int,
                size: Int,
            ): PagedResponse<ProductResponse> {
                return productSearchUseCase.getMainPageProducts(page, size)
            }
        }
    }
}
