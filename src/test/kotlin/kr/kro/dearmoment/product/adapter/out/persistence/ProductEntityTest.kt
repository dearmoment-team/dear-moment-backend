package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import java.time.LocalDateTime
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toKotlinDuration

class ProductEntityTest : StringSpec({

    "ProductEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        // shootingTime: 도메인에서는 kotlin.time.Duration 타입
        val shootingDuration: Duration = 10.toDuration(DurationUnit.MINUTES)

        val partnerShops = listOf(
            PartnerShop("상점1", "http://shop1.com"),
            PartnerShop("상점2", "http://shop2.com")
        )

        // FULL 제공인 경우: partialOriginalCount는 null
        val product = Product(
            productId = 1L,
            userId = 123L,
            title = "테스트 제품",
            description = "이것은 테스트 제품입니다",
            price = 1000L,
            typeCode = 1,
            concept = ConceptType.ELEGANT,
            originalProvideType = OriginalProvideType.FULL,
            partialOriginalCount = null,
            shootingTime = shootingDuration,
            shootingLocation = "테스트 장소",
            numberOfCostumes = 5,
            seasonYear = 2023,
            seasonHalf = null,
            partnerShops = partnerShops,
            detailedInfo = "상세 정보",
            warrantyInfo = "1년 보증",
            contactInfo = "test@example.com",
            createdAt = createdAt,
            updatedAt = createdAt,
            options = emptyList(),
            images = listOf(
                Image(imageId = 0L, userId = 123L, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                Image(imageId = 0L, userId = 123L, fileName = "image2.jpg", url = "http://example.com/image2.jpg")
            )
        )

        val productEntity = ProductEntity.fromDomain(product)
        productEntity.productId shouldBe product.productId
        productEntity.userId shouldBe product.userId
        productEntity.title shouldBe product.title
        productEntity.description shouldBe product.description
        productEntity.price shouldBe product.price
        productEntity.typeCode shouldBe product.typeCode

        // shootingTime은 엔티티에서는 java.time.Duration 타입이므로, toKotlinDuration()을 통해 비교
        productEntity.shootingTime?.toKotlinDuration() shouldBe shootingDuration

        productEntity.shootingLocation shouldBe product.shootingLocation
        productEntity.numberOfCostumes shouldBe product.numberOfCostumes
        productEntity.partnerShops.map { it.name } shouldContainExactlyInAnyOrder partnerShops.map { it.name }
        productEntity.partnerShops.map { it.link } shouldContainExactlyInAnyOrder partnerShops.map { it.link }
        productEntity.detailedInfo shouldBe product.detailedInfo
        productEntity.warrantyInfo shouldBe product.warrantyInfo
        productEntity.contactInfo shouldBe product.contactInfo

        // 새로운 필드 검증
        productEntity.concept shouldBe product.concept
        productEntity.originalProvideType shouldBe product.originalProvideType
        productEntity.partialOriginalCount shouldBe product.partialOriginalCount
        productEntity.createdDate shouldBe product.createdAt
        productEntity.updateDate shouldBe product.updatedAt

        // 이미지 매핑 검증 (fileName 비교)
        productEntity.images.map { it.fileName } shouldContainExactlyInAnyOrder product.images.map { it.fileName }

        // 각 ImageEntity에 product 필드가 올바르게 설정되었는지 검증
        productEntity.images.forEach { it.product shouldBe productEntity }
    }

    "ProductEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        val createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        // 엔티티에서는 shootingTime은 java.time.Duration 타입
        val shootingDurationEntity: JavaDuration = JavaDuration.ofMinutes(10)

        // PartnerShopEmbeddable은 엔티티 변환 시 사용한 값 타입입니다.
        val partnerShops = listOf(
            PartnerShopEmbeddable("상점1", "http://shop1.com"),
            PartnerShopEmbeddable("상점2", "http://shop2.com")
        )

        // PARTIAL 제공인 경우: partialOriginalCount는 3
        // 이미지 필드는 ImageEntity 객체로 생성 (인스턴스 메서드 toDomain() 사용)
        val imageEntity1 = ImageEntity.from(
            Image(
                imageId = 0L,
                userId = 123L,
                fileName = "image1.jpg",
                url = "http://example.com/image1.jpg"
            )
        )
        val imageEntity2 = ImageEntity.from(
            Image(
                imageId = 0L,
                userId = 123L,
                fileName = "image2.jpg",
                url = "http://example.com/image2.jpg"
            )
        )
        // DB 저장 전이므로 createdDate와 updateDate는 null일 수 있음.
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 123L,
            title = "테스트 제품",
            description = "이것은 테스트 제품입니다",
            price = 1000L,
            typeCode = 1,
            concept = ConceptType.ELEGANT,
            originalProvideType = OriginalProvideType.PARTIAL,
            partialOriginalCount = 3,
            shootingTime = shootingDurationEntity,
            shootingLocation = "테스트 장소",
            numberOfCostumes = 5,
            seasonYear = 2023,
            seasonHalf = null,
            partnerShops = partnerShops,
            detailedInfo = "상세 정보",
            warrantyInfo = "1년 보증",
            contactInfo = "test@example.com",
            images = mutableListOf(imageEntity1, imageEntity2),
            version = 0L,
        )

        productEntity.createdDate shouldBe null
        productEntity.updateDate shouldBe null

        val product = productEntity.toDomain()
        product.productId shouldBe productEntity.productId
        product.userId shouldBe productEntity.userId
        product.title shouldBe productEntity.title
        product.description shouldBe productEntity.description
        product.price shouldBe productEntity.price
        product.typeCode shouldBe productEntity.typeCode

        // 엔티티 shootingTime은 java.time.Duration -> 도메인에서는 kotlin.time.Duration 타입으로 변환됨
        product.shootingTime shouldBe shootingDurationEntity.toKotlinDuration()

        product.shootingLocation shouldBe productEntity.shootingLocation
        product.numberOfCostumes shouldBe productEntity.numberOfCostumes
        product.partnerShops.map { it.name } shouldContainExactlyInAnyOrder partnerShops.map { it.name }
        product.partnerShops.map { it.link } shouldContainExactlyInAnyOrder partnerShops.map { it.link }
        product.detailedInfo shouldBe productEntity.detailedInfo
        product.warrantyInfo shouldBe productEntity.warrantyInfo
        product.contactInfo shouldBe productEntity.contactInfo

        product.concept shouldBe productEntity.concept
        product.originalProvideType shouldBe productEntity.originalProvideType
        product.partialOriginalCount shouldBe productEntity.partialOriginalCount
        product.seasonYear shouldBe productEntity.seasonYear
        product.seasonHalf shouldBe productEntity.seasonHalf

        product.createdAt shouldBe null
        product.updatedAt shouldBe null

        // 이미지 도메인 변환 검증 (fileName 비교)
        product.images.map { it.fileName } shouldContainExactlyInAnyOrder productEntity.images.map { it.fileName }
    }
})
