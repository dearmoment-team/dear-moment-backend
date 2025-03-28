package kr.kro.dearmoment.product.application.usecase.get

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
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
    val useCase = GetProductUseCaseImpl(getProductPort)

    // dummy user ID
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    // 더미 이미지 생성 (userId: dummyUserId)
    val dummyImage =
        Image(
            userId = dummyUserId,
            fileName = "dummy.jpg",
            url = "http://example.com/dummy.jpg",
        )

    // 더미 상품 객체 생성 (userId: dummyUserId)
    val dummyProduct =
        Product(
            productId = 1L,
            userId = dummyUserId,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Unique Product",
            description = "Test Description",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = dummyImage,
            subImages = List(4) { dummyImage },
            additionalImages = listOf(dummyImage),
            detailedInfo = "Test Info",
            contactInfo = "Test Contact",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    Given("유효한 상품 ID가 주어졌을 때") {
        When("해당 상품이 존재하는 경우") {
            every { getProductPort.findById(1L) } returns dummyProduct

            Then("정상적으로 상품 정보를 반환해야 한다") {
                val result = useCase.getProductById(1L)
                result shouldBe ProductResponse.fromDomain(dummyProduct)
                verify(exactly = 1) { getProductPort.findById(1L) }
            }
        }
    }

    Given("존재하지 않는 상품 ID가 주어졌을 때") {
        When("해당 상품이 존재하지 않는 경우") {
            every { getProductPort.findById(2L) } returns null

            Then("CustomException 예외를 발생시켜야 한다") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.getProductById(2L)
                    }
                exception shouldHaveMessage ErrorCode.PRODUCT_NOT_FOUND.message
                verify(exactly = 1) { getProductPort.findById(2L) }
            }
        }
    }
})
