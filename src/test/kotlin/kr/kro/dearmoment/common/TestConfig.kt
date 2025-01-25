package kr.kro.dearmoment.common

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
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * 테스트 환경에서 필요한 Mock 및 실제 Bean을 제공하는 설정 클래스입니다.
 */
@Configuration
class TestConfig : AbstractProjectConfig() {

    override fun extensions() = listOf(SpringExtension)

    /**
     * `ProductEntityRetrievalPort`를 Mock으로 설정하여 데이터베이스에서 `ProductEntity`를 조회하는 작업을 시뮬레이션합니다.
     */
    @Bean
    fun productEntityRetrievalPort(jpaProductRepository: JpaProductRepository): ProductEntityRetrievalPort {
        return mockk<ProductEntityRetrievalPort>().apply {
            every { getProductEntityById(any()) } answers {
                val id = firstArg<Long>()
                jpaProductRepository.findById(id)
                    .orElseThrow { IllegalArgumentException("Product with ID $id not found") }
            }
        }
    }

    /**
     * `ProductPersistencePort`의 Adapter를 생성하여 데이터베이스와 상호작용할 수 있도록 설정합니다.
     */
    @Bean
    fun productPersistencePort(jpaProductRepository: JpaProductRepository): ProductPersistencePort {
        return ProductRepositoryAdapter(jpaProductRepository)
    }

    /**
     * `ProductOptionPersistencePort`를 Mock으로 설정하여 `ProductOption` 저장 작업을 시뮬레이션합니다.
     * 저장 시 `name`이 null 또는 빈 문자열인지 검증합니다.
     */
    @Bean
    @Primary
    fun productOptionPersistencePort(
        jpaProductOptionRepository: JpaProductOptionRepository,
        productEntityRetrievalPort: ProductEntityRetrievalPort,
    ): ProductOptionPersistencePort {
        return mockk<ProductOptionPersistencePort>().apply {
            every { save(any()) } answers {
                val option = firstArg<ProductOption>()
                require(!option.name.isNullOrBlank()) { "Mock detected null or blank name in ProductOption" }
                option
            }
        }
    }

    /**
     * `ProductOptionRepositoryAdapter`를 생성하여 데이터베이스와의 상호작용을 지원합니다.
     */
    @Bean
    fun productOptionRepositoryAdapter(
        jpaProductOptionRepository: JpaProductOptionRepository,
        productEntityRetrievalPort: ProductEntityRetrievalPort
    ): ProductOptionRepositoryAdapter {
        return ProductOptionRepositoryAdapter(
            jpaProductOptionRepository = jpaProductOptionRepository,
            productEntityRetrievalPort = productEntityRetrievalPort,
        )
    }

    /**
     * `ProductUseCase`를 생성하여 애플리케이션 비즈니스 로직을 실행할 수 있도록 설정합니다.
     */
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
