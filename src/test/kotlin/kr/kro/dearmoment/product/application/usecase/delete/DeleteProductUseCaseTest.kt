package kr.kro.dearmoment.product.application.usecase.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import java.time.LocalDateTime
import java.util.UUID

class DeleteProductUseCaseTest : BehaviorSpec({

    // 목 객체 생성
    val productPersistencePort = mockk<ProductPersistencePort>()
    val imageService = mockk<ImageService>(relaxed = true)
    val getProductPort = mockk<GetProductPort>()

    // 테스트 대상 UseCase (userId를 추가한 새로운 시그니처)
    val useCase = DeleteProductUseCaseImpl(productPersistencePort, getProductPort, imageService)

    // 테스트용 dummy userId (UUID)
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    val otherUserId = UUID.fromString("11111111-2222-3333-4444-555555555555")

    // 테스트용 더미 이미지들 (userId를 UUID로 설정)
    val uniqueImage1 =
        Image(
            imageId = 1L,
            userId = dummyUserId,
            fileName = "img1.jpg",
            url = "http://example.com/img1.jpg",
        )
    val uniqueImage2 =
        Image(
            imageId = 2L,
            userId = dummyUserId,
            fileName = "img2.jpg",
            url = "http://example.com/img2.jpg",
        )
    val uniqueImage3 =
        Image(
            imageId = 3L,
            userId = dummyUserId,
            fileName = "img3.jpg",
            url = "http://example.com/img3.jpg",
        )
    val uniqueImage4 =
        Image(
            imageId = 4L,
            userId = dummyUserId,
            fileName = "img4.jpg",
            url = "http://example.com/img4.jpg",
        )

    // 상품 객체 (소유자가 dummyUserId인 경우)
    val productDistinct =
        Product(
            productId = 1L,
            userId = dummyUserId,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Unique Product",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = uniqueImage1,
            subImages = listOf(uniqueImage2, uniqueImage3, uniqueImage4, uniqueImage1),
            additionalImages = listOf(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    Given("유효한 상품 ID가 주어졌을 때 (소유자가 인증된 사용자)") {
        When("상품에 고유 이미지들이 포함되어 있는 경우") {
            every { getProductPort.findById(1L) } returns productDistinct
            every { productPersistencePort.deleteById(1L) } returns Unit

            Then("삭제가 수행되어야 한다") {
                useCase.deleteProduct(dummyUserId, 1L)
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
            }
        }
    }

    Given("존재하지 않는 상품 ID가 주어졌을 때") {
        When("상품이 존재하지 않는 경우") {
            every { getProductPort.findById(3L) } returns null

            Then("PRODUCT_NOT_FOUND 예외가 발생해야 한다") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.deleteProduct(dummyUserId, 3L)
                    }
                exception shouldHaveMessage ErrorCode.PRODUCT_NOT_FOUND.message
            }
        }
    }

    Given("상품의 소유자가 인증된 사용자와 다를 때") {
        // 상품의 userId가 다른 사용자인 경우
        val productOtherUser = productDistinct.copy(userId = otherUserId)
        When("인증된 사용자(dummyUserId)가 상품의 소유자가 아닐 경우") {
            every { getProductPort.findById(1L) } returns productOtherUser

            Then("UNAUTHORIZED_ACCESS 예외가 발생해야 한다") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.deleteProduct(dummyUserId, 1L)
                    }
                exception shouldHaveMessage ErrorCode.UNAUTHORIZED_ACCESS.message
            }
        }
    }
})
