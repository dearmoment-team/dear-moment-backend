package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.TestConfig
import kr.kro.dearmoment.common.TestObjectFactory
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

/**
 * ProductEntityRetrievalAdapterTest는 ProductEntityRetrievalAdapter 클래스의 동작을 검증하는 테스트 클래스입니다.
 */
@DataJpaTest
@Import(TestConfig::class)
class ProductEntityRetrievalAdapterTest(
    private val productEntityRetrievalPort: ProductEntityRetrievalPort, // TestConfig에서 정의된 Port 사용
    private val testObjectFactory: TestObjectFactory // 공통 객체 생성 유틸리티
) : StringSpec({

    "특정 ID로 엔티티를 조회한다" {
        // Given
        val fixedNow = LocalDateTime.now()
        val productEntity = testObjectFactory.createTestProductEntity(fixedNow)
        val savedEntity = testObjectFactory.saveProductEntity(productEntity)

        // When
        val retrievedEntity = productEntityRetrievalPort.getProductById(
            requireNotNull(savedEntity.productId) { "Product ID must not be null" }
        )

        // Then
        retrievedEntity.productId shouldBe savedEntity.productId
        retrievedEntity.title shouldBe savedEntity.title
        retrievedEntity.price shouldBe savedEntity.price
    }

    "존재하지 않는 ID로 조회 시 예외가 발생한다" {
        // Given
        val nonExistentId = 999L

        // When & Then
        val exception = shouldThrow<IllegalArgumentException> {
            productEntityRetrievalPort.getProductById(nonExistentId)
        }
        exception.message shouldBe "Product with ID $nonExistentId not found"
    }

    "엔티티 저장 후 필드값을 확인한다" {
        // Given
        val fixedNow = LocalDateTime.now()
        val productEntity = testObjectFactory.createTestProductEntity(fixedNow)
        val savedEntity = testObjectFactory.saveProductEntity(productEntity)

        // Option 도메인 생성 및 저장
        val productOption = testObjectFactory.createTestProductOptionDomain(
            fixedNow = fixedNow,
            productId = requireNotNull(savedEntity.productId) { "Product ID must not be null" },
            name = "옵션 테스트",
            additionalPrice = 1000L
        )
        val savedOption = testObjectFactory.saveProductOption(productOption)

        // When
        val retrievedEntity = productEntityRetrievalPort.getProductById(
            requireNotNull(savedEntity.productId) { "Product ID must not be null" }
        )

        // Then
        retrievedEntity.productId shouldBe savedEntity.productId
        retrievedEntity.title shouldBe savedEntity.title
        retrievedEntity.options.size shouldBe 1
        retrievedEntity.options.first().name shouldBe savedOption.name
    }

})


