package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import java.time.LocalDateTime
import java.util.UUID

internal class ProductEntityTest : StringSpec({

    // dummy userId를 UUID로 선언
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    "ProductEntity는 도메인 모델에서 옵션을 포함하여 올바르게 변환되어야 한다" {
        // given
        // 더미 옵션 객체 생성
        val dummyOption =
            ProductOption(
                optionId = 10L,
                productId = 1L,
                name = "옵션 테스트",
                optionType = OptionType.SINGLE,
                discountAvailable = false,
                originalPrice = 10000,
                discountPrice = 9000,
                description = "옵션 설명",
                costumeCount = 1,
                shootingLocationCount = 1,
                shootingHours = 2,
                shootingMinutes = 30,
                retouchedCount = 1,
                originalProvided = true,
                partnerShops = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // 도메인 Product 객체 생성 (옵션 포함)
        val product =
            Product(
                productId = 1L,
                userId = dummyUserId,
                productType = ProductType.WEDDING_SNAP,
                shootingPlace = ShootingPlace.JEJU,
                title = "테스트 제품",
                description = "테스트 설명",
                availableSeasons =
                    setOf(
                        ShootingSeason.YEAR_2025_FIRST_HALF,
                        ShootingSeason.YEAR_2025_SECOND_HALF,
                    ),
                cameraTypes = setOf(CameraType.DIGITAL),
                retouchStyles = setOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE),
                mainImage =
                    Image(
                        userId = dummyUserId,
                        fileName = "main.jpg",
                        url = "http://example.com/main.jpg",
                    ),
                subImages =
                    listOf(
                        Image(userId = dummyUserId, fileName = "sub1.jpg", url = "http://example.com/sub1.jpg"),
                        Image(userId = dummyUserId, fileName = "sub2.jpg", url = "http://example.com/sub2.jpg"),
                        Image(userId = dummyUserId, fileName = "sub3.jpg", url = "http://example.com/sub3.jpg"),
                        Image(userId = dummyUserId, fileName = "sub4.jpg", url = "http://example.com/sub4.jpg"),
                    ),
                additionalImages =
                    listOf(
                        Image(userId = dummyUserId, fileName = "add1.jpg", url = "http://example.com/add1.jpg"),
                        Image(userId = dummyUserId, fileName = "add2.jpg", url = "http://example.com/add2.jpg"),
                    ),
                detailedInfo = "상세 정보",
                contactInfo = "연락처 정보",
                options = listOf(dummyOption),
            )

        // when: 도메인 모델로부터 엔티티 변환
        val productEntity = ProductEntity.fromDomain(product, studioEntityFixture())

        // then
        productEntity.productId shouldBe 1L
        productEntity.userId shouldBe dummyUserId
        productEntity.productType shouldBe ProductType.WEDDING_SNAP
        productEntity.shootingPlace shouldBe ShootingPlace.JEJU
        productEntity.title shouldBe "테스트 제품"
        productEntity.description shouldBe "테스트 설명"

        productEntity.availableSeasons shouldContainExactly
            setOf(
                ShootingSeason.YEAR_2025_FIRST_HALF,
                ShootingSeason.YEAR_2025_SECOND_HALF,
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

        // 옵션 리스트 변환 검증
        productEntity.options.size shouldBe 1
        productEntity.options[0].name shouldBe "옵션 테스트"
    }
})
