package kr.kro.dearmoment.product.adapter.input.web

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
}
