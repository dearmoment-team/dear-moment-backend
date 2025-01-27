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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * [SpringBootTest 통합 테스트]
 * - 상품 및 옵션 저장 중간에 실패가 발생했을 때 트랜잭션이 롤백되는 시나리오를 검증합니다.
 */
@SpringBootTest
@Transactional
class ProductUseCaseIntegrationTest @Autowired constructor(
    private val productUseCase: ProductUseCase,
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort
) : BehaviorSpec({

    Given("ProductUseCase의 saveProduct 메서드") {
        When("옵션 저장이 실패하면 트랜잭션이 롤백됩니다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val product = Product(
                userId = 1L,
                title = "새 상품",
                description = "새 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        productId = null, // 새 Product이므로 null
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            // Mock 설정: ProductPersistencePort.save 호출 시 productId를 할당한 Product를 반환
            val savedProduct = product.copy(
                productId = 1L,
                options = product.options.map { it.copy(productId = 1L) }
            )

            every { productPersistencePort.save(any()) } returns savedProduct

            // Mock 설정: 옵션 저장 실패를 강제하기 위해 예외를 발생시킴
            every { productOptionPersistencePort.save(any()) } throws RuntimeException("옵션 저장 실패")

            // when & then
            val exception = shouldThrow<RuntimeException> {
                productUseCase.saveProduct(product)
            }

            // 예외 메시지가 올바른지 확인
            exception.message shouldBe "옵션 저장 중 문제가 발생했습니다: 옵션 저장 실패"

            // 트랜잭션 롤백 확인:
            // "새 상품"이라는 제목의 상품이 DB에 없어야 함
            val allProducts = productPersistencePort.findAll()
            allProducts.none { it.title == "새 상품" } shouldBe true
        }
    }
})
