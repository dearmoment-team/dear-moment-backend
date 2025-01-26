package kr.kro.dearmoment.common

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.product.adapter.out.persistence.*
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
     * JPA 관련 빈 생성
     */
    @Bean
    fun productEntityRetrievalPort(jpaProductRepository: JpaProductRepository): ProductEntityRetrievalPort {
        return ProductEntityRetrievalAdapter(jpaProductRepository)
    }

    @Bean
    fun productPersistencePort(jpaProductRepository: JpaProductRepository): ProductPersistencePort {
        return ProductPersistenceAdapter(jpaProductRepository)
    }

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
     * Mock 설정 - ProductOptionPersistencePort
     */
    @Bean
    @Primary
    fun productOptionPersistencePort(): ProductOptionPersistencePort {
        return mockk<ProductOptionPersistencePort>().apply {
            every { save(any()) } answers {
                val option = firstArg<ProductOption>()
                require(!option.name.isNullOrBlank()) { "Mock detected null or blank name in ProductOption" }
                option
            }
        }
    }

    /**
     * ProductUseCase Bean 생성
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

    /**
     * 공통 객체 생성 유틸리티
     */
    @Bean
    fun testObjectFactory(
        jpaProductRepository: JpaProductRepository,
        jpaProductOptionRepository: JpaProductOptionRepository
    ) = TestObjectFactory(jpaProductRepository, jpaProductOptionRepository)
}
