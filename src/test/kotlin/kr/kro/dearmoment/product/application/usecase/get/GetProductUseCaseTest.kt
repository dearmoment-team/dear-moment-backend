package kr.kro.dearmoment.product.application.usecase.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.common.fixture.studioFixture
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.product.application.dto.response.GetProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import java.time.LocalDateTime
import java.util.UUID

class GetProductUseCaseTest : BehaviorSpec({

    val getProductPort = mockk<GetProductPort>()
    val getLikePort = mockk<GetLikePort>()
    val useCase = GetProductUseCaseImpl(getProductPort, getLikePort)

    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    val dummyImage =
        Image(
            userId = dummyUserId,
            fileName = "dummy.jpg",
            url = "http://example.com/dummy.jpg",
        )

    val dummyProduct =
        Product(
            productId = 1L,
            userId = dummyUserId,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Unique Product",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = dummyImage,
            subImages = List(4) { dummyImage },
            additionalImages = listOf(dummyImage),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
            studio = studioFixture(userId = dummyUserId),
        )

    Given("getProductById를 호출할 때") {

        When("해당 상품이 존재하는 경우") {
            val productLikeId = 0L
            val randomUserId = UUID.randomUUID()

            every { getProductPort.findWithStudioById(dummyProduct.productId) } returns dummyProduct
            every { getLikePort.findProductLikesByUserIdAndProductId(randomUserId, dummyProduct.productId) } returns null
            every { getLikePort.findOptionLikesByUserIdAndOptionIds(randomUserId, dummyProduct.options.map { it.optionId }) } returns emptyList() // 린트 설정 테스트

            Then("상품 정보를 정상적으로 반환해야 한다") {
                val result = useCase.getProductById(1L, randomUserId)
                result shouldBe GetProductResponse.fromDomain(dummyProduct, productLikeId)

                // 호출 검증
                verify(exactly = 1) { getProductPort.findWithStudioById(1L) }
                verify(exactly = 1) {
                    getLikePort.findProductLikesByUserIdAndProductId(randomUserId, 1L)
                }
                verify(exactly = 1) {
                    // 실제로는 옵션 ID가 empty가 아닐 수도 있으니, dummyProduct.options.map { it.optionId }로 넣으세요
                    getLikePort.findOptionLikesByUserIdAndOptionIds(randomUserId, emptyList())
                }

                // 호출 이력 체크 & 리셋
                confirmVerified(getProductPort, getLikePort)
                clearMocks(getProductPort, getLikePort, answers = false)
            }
        }

        When("상품이 존재하지 않는 경우") {
            every { getProductPort.findWithStudioById(2L) } throws CustomException(ErrorCode.PRODUCT_NOT_FOUND)

            Then("CustomException 예외가 발생해야 한다") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.getProductById(2L, null)
                    }
                exception shouldHaveMessage ErrorCode.PRODUCT_NOT_FOUND.message

                verify(exactly = 1) { getProductPort.findWithStudioById(2L) }

                // LikePort 호출이 없었다면 아래 verify를 추가하거나 생략합니다.
                // verify(exactly = 0) { getLikePort.findProductLikesByUserIdAndProductId(any(), any()) }
                // verify(exactly = 0) { getLikePort.findOptionLikesByUserIdAndOptionIds(any(), any()) }

                confirmVerified(getProductPort, getLikePort)
                clearMocks(getProductPort, getLikePort, answers = false)
            }
        }
    }

    Given("getMyProduct를 호출할 때") {

        When("해당 userId에 매칭되는 상품이 존재하는 경우") {
            // 1. 반환 타입을 List로 변경
            every { getProductPort.findByUserId(dummyUserId) } returns listOf(dummyProduct)

            Then("상품 정보 리스트를 반환해야 한다") {
                val result = useCase.getMyProduct(dummyUserId)
                // 2. 단일 객체 대신 리스트 비교
                result shouldBe listOf(GetProductResponse.fromDomain(dummyProduct))

                // 3. 메서드명 변경 확인
                verify(exactly = 1) { getProductPort.findByUserId(dummyUserId) }
                confirmVerified(getProductPort, getLikePort)
                clearMocks(getProductPort, getLikePort, answers = false)
            }
        }

        When("해당 userId에 매칭되는 상품이 존재하지 않는 경우") {
            // 4. 예외 발생 대신 빈 리스트 반환
            every { getProductPort.findByUserId(dummyUserId) } returns emptyList()

            Then("빈 리스트를 반환해야 한다") {
                val result = useCase.getMyProduct(dummyUserId)
                // 5. 예외 체크 대신 빈 리스트 검증
                result shouldBe emptyList()

                verify(exactly = 1) { getProductPort.findByUserId(dummyUserId) }
                confirmVerified(getProductPort, getLikePort)
                clearMocks(getProductPort, getLikePort, answers = false)
            }
        }
    }
})
