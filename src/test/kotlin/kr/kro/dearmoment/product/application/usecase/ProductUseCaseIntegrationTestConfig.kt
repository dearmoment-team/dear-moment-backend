package kr.kro.dearmoment.product.application.usecase

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import io.mockk.mockk
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort

@TestConfiguration
class ProductUseCaseIntegrationTestConfig {

    @Bean
    fun productOptionPersistencePort(): ProductOptionPersistencePort {
        return mockk()
    }

    // 필요한 경우 다른 포트들도 Mock으로 설정할 수 있습니다.
}