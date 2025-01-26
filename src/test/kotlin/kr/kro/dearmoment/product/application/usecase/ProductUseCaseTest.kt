package kr.kro.dearmoment.product.application.usecase

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kr.kro.dearmoment.common.TestObjectFactory
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

/**
 * ProductUseCaseTest:
 * - ProductUseCase.modifyProductOptions(...) 메서드를 검증하는 테스트.
 *
 * [핵심 포인트]
 * 1) 기존 옵션과 새 옵션을 구분하기 위해 optionId를 명확히 사용해야 함.
 *    - "진짜 DB에 존재하는 기존 옵션" -> optionId가 0이 아닌 실제 값(예: 10L)
 *    - "새로 추가되는 옵션" -> optionId=null (혹은 0L)로 설정
 * 2) 이렇게 해야, UseCase 내부 로직에서
 *    - 기존 옵션이 리스트에서 빠졌는지? → deleteById(기존 옵션의 ID) 호출
 *    - 새 옵션인지? → save(...)만 호출
 *    등이 제대로 동작하게 됨.
 * 3) 만약 기존 옵션조차 optionId=0L로 만들어 버리면,
 *    UseCase가 "이건 새 옵션이네?"라고 간주해버려서
 *    deleteById(0)이 호출될 가능성이 거의 없고,
 *    결국 테스트가 "deleteById(0)를 기대"해도 일어나지 않아 실패하게 됨.
 */
@ExtendWith(MockKExtension::class)
class ProductUseCaseTest : BehaviorSpec({

    // 모의 객체들(Mock) 선언
    val productPersistencePort: ProductPersistencePort = mockk(relaxed = true)
    val productOptionPersistencePort: ProductOptionPersistencePort = mockk(relaxed = true)
    val productEntityRetrievalPort: ProductEntityRetrievalPort = mockk(relaxed = true)

    // 테스트용 Factory - 실제 DB는 연결되지 않았으므로, Mock Repo를 넣어둔 상태
    val factory = TestObjectFactory(mockk(), mockk())

    // 테스트 대상 UseCase
    val productUseCase = ProductUseCase(
        productPersistencePort,
        productOptionPersistencePort,
        productEntityRetrievalPort
    )

    // -------------------------- 사전 준비 --------------------------
    // BehaviorSpec에서 여러 When 블록을 쓰면 MockK 호출 기록이 누적될 수 있으므로,
    // 각 테스트 시나리오 전에 clearAllMocks()를 해주는 습관이 좋다.
    beforeEach {
        clearAllMocks()
    }

    Given("ProductUseCase의 modifyProductOptions 메서드") {

        // ----------------------------------------------------------------------
        // 공통으로 쓸 fixedNow와 product 준비
        // productId는 1L로 설정 (실제 DB에 이미 있다고 가정)
        // ----------------------------------------------------------------------
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val product = factory.createTestProductDomain(
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = emptyList()
        ).copy(productId = 1L)

        // ----------------------------------------------------------------------
        // "기존 옵션"이라고 가정하는 경우, DB에 이미 존재하는 PK가 있어야 하므로
        // optionId는 10L처럼 0이 아닌 값을 준다.
        // "새 옵션"이라고 가정하는 경우, optionId=null 또는 0L을 준다.
        // ----------------------------------------------------------------------

        // 기존에 존재하던 옵션 (예: ID=10L)
        val existingOption = factory.createTestProductOptionDomain(
            fixedNow = fixedNow,
            productId = product.productId,
            name = "Existing Option",
            additionalPrice = 5000L
        ).copy(optionId = 10L) // ★ 기존 옵션이므로 10L 같은 양의 값

        // 새로 추가될 옵션
        // factory.createTestProductOptionDomain은 기본적으로 optionId=0L을 넣으므로
        // 새 레코드(== DB에 없는 옵션)라고 간주 가능.
        val newOption = factory.createTestProductOptionDomain(
            fixedNow = fixedNow,
            productId = product.productId,
            name = "New Option",
            additionalPrice = 5000L
        ).copy(optionId = null) // 명시적으로 null 처리 - "새 엔티티"임을 표현

        // ----------------------------------------------------------------------
        // 시나리오 1) "옵션이 새로 추가된 경우 저장된다"
        // ----------------------------------------------------------------------
        When("옵션이 새로 추가된 경우 저장된다") {
            // 가정: DB에서 찾은 product는 'options'가 비어 있다고 하고,
            // findByProduct(...) 또한 빈 리스트라고 가정
            every { productEntityRetrievalPort.getProductById(1L) } returns product
            every { productOptionPersistencePort.findByProduct(any()) } returns emptyList()

            // 실제로 새 옵션을 추가
            productUseCase.modifyProductOptions(1L, listOf(newOption))

            // 이 때, "새로 추가된 경우"를 UseCase는 save(...)만 호출할 것이다
            verify(exactly = 1) {
                productOptionPersistencePort.save(eq(newOption))
            }
            // 삭제는 일어나지 않아야 한다
            verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
        }

        // ----------------------------------------------------------------------
        // 시나리오 2) "여러 옵션이 동시에 추가, 업데이트, 삭제되는 경우"
        // ----------------------------------------------------------------------
        When("여러 옵션이 동시에 추가, 업데이트, 삭제되는 경우") {

            // 먼저, DB 상에 'existingOption' (optionId=10L)이라는 옵션이 존재한다고 가정하자
            // 또 다른 기존 옵션도 삭제해 보려면, 아래처럼 하나 더 만든 뒤 ID=20L을 부여할 수도 있음
            val existingOptionToDelete = factory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = product.productId,
                name = "Option to Delete",
                additionalPrice = 3000L
            ).copy(optionId = 20L) // ★ 삭제 대상 옵션이므로 ID=20L

            // updatedOption: 기존 옵션(10L)을 업데이트하는 시나리오
            val updatedOption = existingOption.copy(
                name = "Updated Option Name",
                additionalPrice = 6000L
            )
            // newOption2: 새로 추가할 옵션
            val newOption2 = factory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = product.productId,
                name = "New Option 2",
                additionalPrice = 8000L
            ).copy(optionId = null)

            // productEntityRetrievalPort.getProductById(1L)를 호출하면
            // "product"가 반환된다고 세팅
            every { productEntityRetrievalPort.getProductById(1L) } returns product

            // productOptionPersistencePort.findByProduct(product) 호출 시
            // "기존 옵션 2개(10L, 20L) 존재"한다고 반환
            every { productOptionPersistencePort.findByProduct(product) } returns listOf(
                existingOption,
                existingOptionToDelete
            )

            // 이제 UseCase에 "updatedOption, newOption2"만 남겨놓는다고 지시
            productUseCase.modifyProductOptions(1L, listOf(updatedOption, newOption2))

            // ----- 검증 1) 'updatedOption'이 업데이트(=save) 되었는지 -----
            verify(exactly = 1) {
                productOptionPersistencePort.save(
                    match {
                        it.optionId == 10L && // 기존 옵션 ID 그대로
                                it.name == "Updated Option Name" &&
                                it.additionalPrice == 6000L &&
                                it.productId == 1L
                    }
                )
            }
            // ----- 검증 2) 'newOption2'가 새 옵션(=save) 되었는지 -----
            verify(exactly = 1) {
                productOptionPersistencePort.save(
                    match {
                        it.optionId == null && // 새 옵션이므로 null
                                it.name == "New Option 2" &&
                                it.additionalPrice == 8000L &&
                                it.productId == 1L
                    }
                )
            }
            // ----- 검증 3) 기존에 있던 'existingOptionToDelete' (ID=20L)가 삭제(deleteById) 되었는지 -----
            verify(exactly = 1) {
                productOptionPersistencePort.deleteById(20L)
            }
        }
    }
})
