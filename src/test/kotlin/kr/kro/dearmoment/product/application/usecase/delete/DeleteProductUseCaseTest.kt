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

    // 더미 이미지 객체 (imageId 포함)
    val dummyImage =
        Image(
            imageId = 1L,
            userId = 1L,
            fileName = "dummy.jpg",
            url = "http://example.com/dummy.jpg",
        )

    // 더미 상품 객체 생성 (메인, 서브, 추가 이미지 포함)
    val dummyProduct =
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
            every { productPersistencePort.findById(1L) } returns dummyProduct
            every { productPersistencePort.deleteById(1L) } returns Unit

            Then("모든 관련 이미지 삭제 후 상품 삭제가 수행되어야 한다") {
                useCase.deleteProduct(1L)

                verify(exactly = 6) { imageService.delete(dummyImage.imageId) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
            }
        }
    }

    Given("존재하지 않는 상품 ID가 주어졌을 때") {
        When("상품이 존재하지 않는 경우") {
            every { productPersistencePort.findById(2L) } returns null

            Then("IllegalArgumentException 예외가 발생해야 한다") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.deleteProduct(2L)
                    }
                exception.message shouldBe "삭제할 상품이 존재하지 않습니다. ID: 2"
            }
        }
    }
})
