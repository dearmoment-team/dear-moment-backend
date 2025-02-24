package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import java.time.LocalDateTime

class ProductEntityTest : StringSpec({

    /**
     * ProductEntity.fromDomain(...) 테스트
     * - 도메인 Product -> ProductEntity 로 변환되는 로직 검증
     */
    "ProductEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        // given
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        // 도메인 모델 설정
        val product = Product(
            productId = 1L,
            userId = 123L,
            title = "테스트 제품",
            description = "테스트 설명",
            basePrice = 1000L,
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF, ShootingSeason.YEAR_2025_SECOND_HALF),
            cameraTypes = setOf(CameraType.DIGITAL),
            retouchStyles = setOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE),
            mainImage = Image(
                imageId = 100L,
                userId = 123L,
                parId = "MAIN",
                fileName = "main.jpg",
                url = "http://example.com/main.jpg"
            ),
            // 서브 이미지는 4장 필수
            subImages = listOf(
                Image(
                    imageId = 101L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub1.jpg",
                    url = "http://example.com/sub1.jpg"
                ),
                Image(
                    imageId = 102L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub2.jpg",
                    url = "http://example.com/sub2.jpg"
                ),
                Image(
                    imageId = 103L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub3.jpg",
                    url = "http://example.com/sub3.jpg"
                ),
                Image(
                    imageId = 104L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub4.jpg",
                    url = "http://example.com/sub4.jpg"
                )
            ),
            // 추가 이미지는 최대 5장
            additionalImages = listOf(
                Image(
                    imageId = 105L,
                    userId = 123L,
                    parId = "ADD",
                    fileName = "add1.jpg",
                    url = "http://example.com/add1.jpg"
                ),
                Image(
                    imageId = 106L,
                    userId = 123L,
                    parId = "ADD",
                    fileName = "add2.jpg",
                    url = "http://example.com/add2.jpg"
                )
            ),
            detailedInfo = "상세 정보",
            contactInfo = "연락처 정보",
            createdAt = createdAt,
            updatedAt = updatedAt,
            // 옵션은 여기서는 테스트 생략 (빈 리스트)
            options = emptyList()
        )

        // when
        val productEntity = ProductEntity.fromDomain(product)

        // then
        productEntity.productId shouldBe 1L
        productEntity.userId shouldBe 123L
        productEntity.title shouldBe "테스트 제품"
        productEntity.description shouldBe "테스트 설명"
        productEntity.basePrice shouldBe 1000L

        productEntity.availableSeasons shouldContainExactly setOf(
            ShootingSeason.YEAR_2025_FIRST_HALF,
            ShootingSeason.YEAR_2025_SECOND_HALF
        )
        productEntity.cameraTypes shouldContainExactly setOf(CameraType.DIGITAL)
        productEntity.retouchStyles shouldContainExactly setOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE)

        productEntity.mainImage.run {
            imageId shouldBe 100L
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

        productEntity.createdAt shouldBe createdAt
        productEntity.updatedAt shouldBe updatedAt

        // 옵션(entities) 확인
        productEntity.options.size shouldBe 0
    }

    /**
     * ProductEntity.toDomain(...) 테스트
     * - ProductEntity -> 도메인 Product 로 변환되는 로직 검증
     */
    "ProductEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        // given
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val updatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val productEntity = ProductEntity(
            productId = 1L,
            userId = 123L,
            title = "테스트 제품",
            description = "테스트 설명",
            basePrice = 1000L,
            availableSeasons = mutableSetOf(
                ShootingSeason.YEAR_2025_FIRST_HALF,
                ShootingSeason.YEAR_2025_SECOND_HALF
            ),
            cameraTypes = mutableSetOf(CameraType.DIGITAL),
            retouchStyles = mutableSetOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE),
            mainImage = ImageEmbeddable(
                imageId = 100L,
                userId = 123L,
                parId = "MAIN",
                fileName = "main.jpg",
                url = "http://example.com/main.jpg"
            ),
            subImages = mutableListOf(
                ImageEmbeddable(
                    imageId = 101L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub1.jpg",
                    url = "http://example.com/sub1.jpg"
                ),
                ImageEmbeddable(
                    imageId = 102L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub2.jpg",
                    url = "http://example.com/sub2.jpg"
                ),
                ImageEmbeddable(
                    imageId = 103L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub3.jpg",
                    url = "http://example.com/sub3.jpg"
                ),
                ImageEmbeddable(
                    imageId = 104L,
                    userId = 123L,
                    parId = "SUB",
                    fileName = "sub4.jpg",
                    url = "http://example.com/sub4.jpg"
                )
            ),
            additionalImages = mutableListOf(
                ImageEmbeddable(
                    imageId = 105L,
                    userId = 123L,
                    parId = "ADD",
                    fileName = "add1.jpg",
                    url = "http://example.com/add1.jpg"
                ),
                ImageEmbeddable(
                    imageId = 106L,
                    userId = 123L,
                    parId = "ADD",
                    fileName = "add2.jpg",
                    url = "http://example.com/add2.jpg"
                )
            ),
            detailedInfo = "상세 정보",
            contactInfo = "연락처 정보",
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // when
        val product = productEntity.toDomain()

        // then
        product.productId shouldBe 1L
        product.userId shouldBe 123L
        product.title shouldBe "테스트 제품"
        product.description shouldBe "테스트 설명"
        product.basePrice shouldBe 1000L

        product.availableSeasons shouldContainExactly setOf(
            ShootingSeason.YEAR_2025_FIRST_HALF,
            ShootingSeason.YEAR_2025_SECOND_HALF
        )
        product.cameraTypes shouldContainExactly setOf(CameraType.DIGITAL)
        product.retouchStyles shouldContainExactly setOf(RetouchStyle.CHIC, RetouchStyle.VINTAGE)

        product.mainImage.run {
            imageId shouldBe 100L
            fileName shouldBe "main.jpg"
            url shouldBe "http://example.com/main.jpg"
        }

        product.subImages.size shouldBe 4
        product.subImages[0].fileName shouldBe "sub1.jpg"
        product.subImages[1].fileName shouldBe "sub2.jpg"
        product.subImages[2].fileName shouldBe "sub3.jpg"
        product.subImages[3].fileName shouldBe "sub4.jpg"

        product.additionalImages.size shouldBe 2
        product.additionalImages[0].fileName shouldBe "add1.jpg"
        product.additionalImages[1].fileName shouldBe "add2.jpg"

        product.detailedInfo shouldBe "상세 정보"
        product.contactInfo shouldBe "연락처 정보"

        product.createdAt shouldBe createdAt
        product.updatedAt shouldBe updatedAt

        // 옵션(domain) 확인
        product.options.size shouldBe 0
    }
})
