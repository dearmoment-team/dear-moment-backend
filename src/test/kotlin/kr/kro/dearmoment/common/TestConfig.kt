package kr.kro.dearmoment.common

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kr.kro.dearmoment.product.adapter.out.persistence.*
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * [스프링 컨텍스트 기반 테스트를 위한 구성]
 *
 * - @TestConfiguration: 테스트용으로만 사용되는 Configuration
 * - 여기에 @Primary로 등록된 Mock을 사용해,
 *   UseCase 테스트에서 deleteAllByProductId, deleteById, save 등의 호출을 검증한다.
 */
@TestConfiguration
class TestConfig : AbstractProjectConfig() {

    override fun extensions() = listOf(SpringExtension)

    /**
     * 실제 JPA Adapters (주로 필요하면)
     */
    @Bean
    fun productEntityRetrievalPort(jpaProductRepository: JpaProductRepository): ProductEntityRetrievalPort {
        return ProductEntityRetrievalAdapter(jpaProductRepository)
    }

    @Bean
    fun productPersistencePort(
        jpaProductRepository: JpaProductRepository,
        jpaProductOptionRepository: JpaProductOptionRepository
    ): ProductPersistencePort {
        return ProductPersistenceAdapter(jpaProductRepository, jpaProductOptionRepository)
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
     * [중요] @Primary Mock Bean
     * - UseCase는 이 Bean을 주입받아서 로직을 수행.
     * - 모든 메서드를 Stub 처리하여 "Verification failed" 문제 해소.
     */
    @Bean
    @Primary
    fun productOptionPersistencePort(): ProductOptionPersistencePort {
        return mockk(relaxed = false) {

            // save(...) Stub
            every { save(any()) } answers {
                val option = firstArg<ProductOption>()
                // 단순 유효성 검사
                require(!option.name.isNullOrBlank()) { "Mock detected blank option name" }
                option
            }

            // deleteById(...) Stub
            every { deleteById(any()) } just Runs

            // deleteAllByProductId(...) Stub
            every { deleteAllByProductId(any()) } just Runs

            // findByProduct(...) Stub
            every { findByProduct(any()) } returns emptyList()

            // findById(...) Stub
            every { findById(any()) } returns mockk(relaxed = true)
        }
    }

    /**
     * UseCase Bean 생성
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
     * 테스트용 Factory
     */
    @Bean
    fun testObjectFactory(
        jpaProductRepository: JpaProductRepository,
        jpaProductOptionRepository: JpaProductOptionRepository
    ) = TestObjectFactory(jpaProductRepository, jpaProductOptionRepository)
}
