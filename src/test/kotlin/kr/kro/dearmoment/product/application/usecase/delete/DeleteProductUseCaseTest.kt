package kr.kro.dearmoment.product.application.usecase.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import java.time.LocalDateTime

class DeleteProductUseCaseTest : BehaviorSpec({

    val productPersistencePort = mockk<ProductPersistencePort>()
    val imageService = mockk<ImageService>(relaxed = true)
    val useCase = DeleteProductUseCaseImpl(productPersistencePort, imageService)

    // 테스트용 더미 이미지들
    val uniqueImage1 =
        Image(
            imageId = 1L,
            userId = 1L,
            fileName = "img1.jpg",
            url = "http://example.com/img1.jpg",
        )
    val uniqueImage2 =
        Image(
            imageId = 2L,
            userId = 1L,
            fileName = "img2.jpg",
            url = "http://example.com/img2.jpg",
        )
    val uniqueImage3 =
        Image(
            imageId = 3L,
            userId = 1L,
            fileName = "img3.jpg",
            url = "http://example.com/img3.jpg",
        )
    val uniqueImage4 =
        Image(
            imageId = 4L,
            userId = 1L,
            fileName = "img4.jpg",
            url = "http://example.com/img4.jpg",
        )

    // 상품 객체 - 고유 이미지들만 포함한 경우
    val productDistinct =
        Product(
            productId = 1L,
            userId = 1L,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Unique Product",
            description = "Test Description",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = uniqueImage1,
            subImages = listOf(uniqueImage2, uniqueImage3, uniqueImage4, uniqueImage1),
            additionalImages = listOf(),
            detailedInfo = "Test Info",
            contactInfo = "Test Contact",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    Given("유효한 상품 ID가 주어졌을 때 (고유 이미지만 포함)") {
        When("상품에 고유 이미지들이 포함되어 있는 경우") {
            every { productPersistencePort.findById(1L) } returns productDistinct
            every { productPersistencePort.deleteById(1L) } returns Unit

            Then("각 이미지에 대해 삭제가 수행되어야 한다") {
                useCase.deleteProduct(1L)
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
            }
        }
    }

    Given("존재하지 않는 상품 ID가 주어졌을 때") {
        When("상품이 존재하지 않는 경우") {
            every { productPersistencePort.findById(3L) } returns null

            Then("IllegalArgumentException 예외가 발생해야 한다") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.deleteProduct(3L)
                    }
                exception.message shouldBe "삭제할 상품이 존재하지 않습니다. ID: 3"
            }
        }
    }
})
