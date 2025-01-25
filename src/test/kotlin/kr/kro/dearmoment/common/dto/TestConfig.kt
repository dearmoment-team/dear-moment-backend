import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionRepositoryAdapter
import kr.kro.dearmoment.product.adapter.out.persistence.ProductRepositoryAdapter
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class TestConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)

    @Bean
    fun productEntityRetrievalPort(jpaProductRepository: JpaProductRepository): ProductEntityRetrievalPort {
        return mockk<ProductEntityRetrievalPort>().apply {
            every { getProductEntityById(any()) } answers {
                val id = firstArg<Long>()
                println("Mock retrieving ProductEntity with id: $id")
                jpaProductRepository.findById(id)
                    .orElseThrow { IllegalArgumentException("Product with ID $id not found") }
            }
        }
    }

    @Bean
    fun productPersistencePort(jpaProductRepository: JpaProductRepository): ProductPersistencePort {
        return ProductRepositoryAdapter(jpaProductRepository)
    }

    @Bean
    @Primary
    fun productOptionPersistencePort(
        jpaProductOptionRepository: JpaProductOptionRepository,
        productEntityRetrievalPort: ProductEntityRetrievalPort,
    ): ProductOptionPersistencePort {
        return ProductOptionRepositoryAdapter(
            jpaProductOptionRepository = jpaProductOptionRepository,
            productEntityRetrievalPort = productEntityRetrievalPort,
        )
    }

    @Bean
    fun productUseCase(
        productPersistencePort: ProductPersistencePort,
        productOptionPersistencePort: ProductOptionPersistencePort,
        productEntityRetrievalPort: ProductEntityRetrievalPort,
    ): ProductUseCase {
        return ProductUseCase(
            productPersistencePort,
            productOptionPersistencePort,
            productEntityRetrievalPort,
        )
    }
}