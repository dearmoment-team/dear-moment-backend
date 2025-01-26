package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class ProductUseCaseIntegrationTest(
    private val productUseCase: ProductUseCase,
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort
) : BehaviorSpec({
    Given("ProductUseCase의 saveProduct 메서드") {
        When("옵션 저장 실패 시 트랜잭션이 롤백된다") {
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

            // Mock 설정
            every { productOptionPersistencePort.save(any()) } throws RuntimeException("옵션 저장 실패")

            // when & then
            val exception = shouldThrow<RuntimeException> {
                productUseCase.saveProduct(product)
            }

            exception.message shouldBe "옵션 저장 중 문제가 발생했습니다: 옵션 저장 실패"

            // 트랜잭션 롤백 확인
            val retrievedProduct = productPersistencePort.findById(product.productId)
            retrievedProduct shouldBe null
        }
    }
})