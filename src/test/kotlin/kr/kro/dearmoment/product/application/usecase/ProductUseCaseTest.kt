package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.verify
import kr.kro.dearmoment.common.TestObjectFactory
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

/**
 * [스프링 컨텍스트 기반 통합 테스트 + Mock 주입]
 *
 * - @SpringBootTest로 스프링 컨텍스트를 띄운 뒤,
 *   TestConfig가 등록한 @Primary Mock Bean을 주입받아 테스트한다.
 * - UseCase는 동일한 Mock Bean을 사용하므로,
 *   verify(...) 검증 시 "Verification failed: call not found" 문제를 방지한다.
 * - deleteAllByProductId, deleteById 등 모든 메서드가 Stub되어 있어야 한다.
 */
/**
 * [스프링 컨텍스트 기반 통합 테스트 + Mock 주입]
 *
 * - @SpringBootTest(classes = [TestConfig::class])는 TestConfig에 정의된 Mock 및 Bean을 주입받도록 설정합니다.
 * - 이 테스트는 @Primary로 설정된 Mock ProductOptionPersistencePort를 사용하는 UseCase를 검증합니다.
 */
@SpringBootTest
class ProductUseCaseTest : BehaviorSpec() {

    // UseCase, Port 인터페이스, TestFactory는 모두 스프링 컨텍스트(TestConfig)에서 가져옵니다.
    @Autowired
    lateinit var productUseCase: ProductUseCase

    @Autowired
    lateinit var productEntityRetrievalPort: ProductEntityRetrievalPort

    @Autowired
    lateinit var productOptionPersistencePort: ProductOptionPersistencePort

    @Autowired
    lateinit var productPersistencePort: ProductPersistencePort

    @Autowired
    lateinit var factory: TestObjectFactory

    init {
        beforeEach {
            clearAllMocks() // 각 테스트 실행 전 Mock 상태 초기화
        }

        //====================================================================
        // 1) modifyProductOptions(...) 메서드 테스트 (부분 옵션 수정)
        //====================================================================
        Given("ProductUseCase의 modifyProductOptions 메서드") {

            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = factory.createTestProductDomain(
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList() // 초기 옵션은 없음
            ).copy(productId = 1L)

            val existingOption = factory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = product.productId!!,
                name = "Existing Option",
                additionalPrice = 5000L
            ).copy(optionId = 10L)

            val newOption = factory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = product.productId!!,
                name = "New Option",
                additionalPrice = 5000L
            ).copy(optionId = null)

            When("옵션이 새로 추가된 경우") {
                every { productEntityRetrievalPort.getProductById(1L) } returns product
                every { productOptionPersistencePort.findByProduct(any()) } returns emptyList()

                productUseCase.modifyProductOptions(1L, listOf(newOption))

                Then("새 옵션이 저장되고, 기존 옵션 삭제는 발생하지 않는다") {
                    verify(exactly = 1) { productOptionPersistencePort.save(newOption) }
                    verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
                }
            }

            When("여러 옵션이 동시에 추가, 업데이트, 삭제되는 경우") {

                val existingOptionToDelete = factory.createTestProductOptionDomain(
                    fixedNow = fixedNow,
                    productId = product.productId!!,
                    name = "Option to Delete",
                    additionalPrice = 3000L
                ).copy(optionId = 20L)

                val updatedOption = existingOption.copy(
                    name = "Updated Option Name",
                    additionalPrice = 6000L
                )

                val newOption2 = factory.createTestProductOptionDomain(
                    fixedNow = fixedNow,
                    productId = product.productId!!,
                    name = "New Option 2",
                    additionalPrice = 8000L
                ).copy(optionId = null)

                every { productEntityRetrievalPort.getProductById(1L) } returns product
                every { productOptionPersistencePort.findByProduct(product) } returns listOf(
                    existingOption,
                    existingOptionToDelete
                )

                productUseCase.modifyProductOptions(1L, listOf(updatedOption, newOption2))

                Then("기존 옵션(10L)이 업데이트된다") {
                    verify(exactly = 1) {
                        productOptionPersistencePort.save(
                            match {
                                it.optionId == 10L &&
                                        it.name == "Updated Option Name" &&
                                        it.additionalPrice == 6000L &&
                                        it.productId == 1L
                            }
                        )
                    }
                }

                Then("새 옵션(null ID)이 저장된다") {
                    verify(exactly = 1) {
                        productOptionPersistencePort.save(
                            match {
                                it.optionId == null &&
                                        it.name == "New Option 2" &&
                                        it.additionalPrice == 8000L &&
                                        it.productId == 1L
                            }
                        )
                    }
                }

                Then("기존 옵션 중 20L는 deleteById로 삭제된다") {
                    verify(exactly = 1) { productOptionPersistencePort.deleteById(20L) }
                }
            }
        }

        //====================================================================
        // 2) deleteProduct(...) 메서드 테스트
        //====================================================================
        Given("ProductUseCase의 deleteProduct 메서드") {

            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val optionToDelete = factory.createTestProductOptionDomain(
                productId = 1L,
                name = "Option to Delete",
                additionalPrice = 5000L,
                fixedNow = fixedNow
            ).copy(optionId = 10L)

            val product = factory.createTestProductDomain(
                userId = 123L,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(optionToDelete)
            ).copy(productId = 1L)

            When("상품을 삭제하면 옵션도 모두 일괄 삭제한다") {
                every { productEntityRetrievalPort.getProductById(1L) } returns product
                every { productOptionPersistencePort.findByProduct(product) } returns listOf(optionToDelete)

                productUseCase.deleteProduct(1L)

                Then("deleteAllByProductId(1L) 호출 후 상품 삭제(deleteById(1L))가 수행된다") {
                    verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                    verify(exactly = 1) { productPersistencePort.deleteById(1L) }
                }
            }

            When("삭제하려는 상품이 존재하지 않을 경우") {
                every { productEntityRetrievalPort.getProductById(1L) } throws IllegalArgumentException("Product not found")

                Then("예외가 발생한다") {
                    val ex = shouldThrow<IllegalArgumentException> {
                        productUseCase.deleteProduct(1L)
                    }
                    ex.message shouldBe "Product not found"
                }
            }
        }
    }
}
