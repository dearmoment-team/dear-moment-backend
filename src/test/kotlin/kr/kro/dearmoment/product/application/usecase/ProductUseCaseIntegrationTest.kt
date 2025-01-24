package kr.kro.dearmoment.product.application.usecase

import io.kotest.core.spec.style.StringSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.context.ActiveProfiles
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime
import io.mockk.every
import kr.kro.dearmoment.product.adapter.out.persistence.ProductRepositoryAdapter
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort

@SpringBootTest
@Import(ProductUseCaseIntegrationTestConfig::class) // 별도의 TestConfiguration 클래스 Import
@Transactional
@ActiveProfiles("test")
class ProductUseCaseIntegrationTest @Autowired constructor(
    private val productUseCase: ProductUseCase,
    private val productRepositoryAdapter: ProductRepositoryAdapter,
    private val productOptionPersistencePort: ProductOptionPersistencePort
) : StringSpec({

    "옵션 저장 실패 시 트랜잭션이 롤백된다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val product = Product(
            productId = 0L,
            userId = 1L,
            title = "새 상품",
            description = "새 상품 설명",
            price = 100000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = listOf(
                ProductOption(
                    optionId = 0L,
                    productId = 0L,
                    name = "옵션 1",
                    additionalPrice = 5000,
                    description = "옵션 1 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )
        )

        // `ProductOptionPersistencePort`의 `save` 메서드가 예외를 던지도록 설정
        every { productOptionPersistencePort.save(any()) } throws RuntimeException("옵션 저장 실패")

        // when & then
        val exception = shouldThrow<RuntimeException> {
            productUseCase.saveProduct(product)
        }
        exception.message shouldBe "옵션 저장 실패"

        // 데이터베이스에 저장된 상품이 없음을 확인
        shouldThrow<IllegalArgumentException> {
            productUseCase.getProductById(1L)
        }.message shouldBe "Product with ID 1 not found"
    }
})