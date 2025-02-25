package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason

class ProductEntityTest : StringSpec({

    "ProductEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        // given
        val product = Product(
            productId = 1L,
            userId = 123L,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "테스트 제품",
            description = "테스트 설명",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF, ShootingSeason.YEAR_2025_SECOND_HALF),
            cameraTypes = setOf(CameraType.DIGITAL),
            retouchStyles = setOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE),
            mainImage = Image(
                userId = 123L,
                fileName = "main.jpg",
                url = "http://example.com/main.jpg"
            ),
            subImages = listOf(
                Image(userId = 123L, fileName = "sub1.jpg", url = "http://example.com/sub1.jpg"),
                Image(userId = 123L, fileName = "sub2.jpg", url = "http://example.com/sub2.jpg"),
                Image(userId = 123L, fileName = "sub3.jpg", url = "http://example.com/sub3.jpg"),
                Image(userId = 123L, fileName = "sub4.jpg", url = "http://example.com/sub4.jpg")
            ),
            additionalImages = listOf(
                Image(userId = 123L, fileName = "add1.jpg", url = "http://example.com/add1.jpg"),
                Image(userId = 123L, fileName = "add2.jpg", url = "http://example.com/add2.jpg")
            ),
            detailedInfo = "상세 정보",
            contactInfo = "연락처 정보",
            options = emptyList()
        )

        // when
        val productEntity = ProductEntity.fromDomain(product)

        // then
        productEntity.productId shouldBe 1L
        productEntity.userId shouldBe 123L
        productEntity.productType shouldBe ProductType.WEDDING_SNAP
        productEntity.shootingPlace shouldBe ShootingPlace.JEJU
        productEntity.title shouldBe "테스트 제품"
        productEntity.description shouldBe "테스트 설명"

        productEntity.availableSeasons shouldContainExactly setOf(
            ShootingSeason.YEAR_2025_FIRST_HALF,
            ShootingSeason.YEAR_2025_SECOND_HALF
        )
        productEntity.cameraTypes shouldContainExactly setOf(CameraType.DIGITAL)
        productEntity.retouchStyles shouldContainExactly setOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE)

        productEntity.mainImage.run {
            fileName shouldBe "main.jpg"
            url shouldBe "http://example.com/main.jpg"
        }

        productEntity.subImages.size shouldBe 4
        productEntity.subImages[0].fileName shouldBe "sub1.jpg"
        productEntity.subImages[1].fileName shouldBe "sub2.jpg"
        productEntity.subImages[2].fileName shouldBe "sub3.jpg"
        productEntity.subImages[3].fileName shouldBe "sub4.jpg"

        productEntity.additionalImages.size shouldBe 2
        productEntity.additionalImages[0].fileName shouldBe "add1.jpg"
        productEntity.additionalImages[1].fileName shouldBe "add2.jpg"

        productEntity.detailedInfo shouldBe "상세 정보"
        productEntity.contactInfo shouldBe "연락처 정보"
    }
})
