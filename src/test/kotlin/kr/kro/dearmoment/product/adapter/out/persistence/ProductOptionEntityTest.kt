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
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import java.time.LocalDateTime
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

class ProductOptionEntityTest : StringSpec({

    "ProductOptionEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        // 생성/수정 시간은 기존 LocalDateTime 사용 (Auditable 관련)
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        // shootingTime은 이제 Duration 타입 (예: 10분)
        val shootingDuration: Duration = 10.toDuration(DurationUnit.MINUTES)
        val partnerShops = listOf(
            PartnerShop("상점1", "http://shop1.com"),
            PartnerShop("상점2", "http://shop2.com"),
        )

        // FULL 제공인 경우: partialOriginalCount는 null
        val productDomain = Product(
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
            createdAt = fixedDateTime,
            updatedAt = fixedDateTime,
            options = emptyList(),
            // images를 List<Image>로 전달
            images = listOf(
                Image(imageId = 0L, userId = 123L, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                Image(imageId = 0L, userId = 123L, fileName = "image2.jpg", url = "http://example.com/image2.jpg"),
            ),
        )

        val productEntity = ProductEntity.fromDomain(productDomain)
        productEntity.productId shouldBe productDomain.productId
        productEntity.userId shouldBe productDomain.userId
        productEntity.title shouldBe productDomain.title
        productEntity.description shouldBe productDomain.description
        productEntity.price shouldBe productDomain.price
        productEntity.typeCode shouldBe productDomain.typeCode

        // productEntity.shootingTime은 java.time.Duration이므로, toKotlinDuration()으로 변환하여 비교
        productEntity.shootingTime?.toKotlinDuration() shouldBe productDomain.shootingTime

        productEntity.shootingLocation shouldBe productDomain.shootingLocation
        productEntity.numberOfCostumes shouldBe productDomain.numberOfCostumes
        productEntity.partnerShops.map { it.name } shouldContainExactlyInAnyOrder partnerShops.map { it.name }
        productEntity.partnerShops.map { it.link } shouldContainExactlyInAnyOrder partnerShops.map { it.link }
        productEntity.detailedInfo shouldBe productDomain.detailedInfo
        productEntity.warrantyInfo shouldBe productDomain.warrantyInfo
        productEntity.contactInfo shouldBe productDomain.contactInfo

        // 새로운 필드들에 대한 검증
        productEntity.concept shouldBe productDomain.concept
        productEntity.originalProvideType shouldBe productDomain.originalProvideType
        productEntity.partialOriginalCount shouldBe productDomain.partialOriginalCount
        productEntity.createdDate shouldBe productDomain.createdAt
        productEntity.updateDate shouldBe productDomain.updatedAt

        // 이미지 매핑 검증: 각 ImageEntity의 fileName과 도메인 Product의 images의 fileName 비교
        productEntity.images.map { it.fileName } shouldContainExactlyInAnyOrder productDomain.images.map { it.fileName }
    }

    "ProductOptionEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        // partnerShops의 엔티티용 값 타입
        val partnerShops = listOf(
            PartnerShopEmbeddable("상점1", "http://shop1.com"),
            PartnerShopEmbeddable("상점2", "http://shop2.com"),
        )
        // shootingTime은 Duration 타입 (예: 10분)
        val shootingDuration: Duration = 10.toDuration(DurationUnit.MINUTES)

        // PARTIAL 제공인 경우: partialOriginalCount는 3
        // 이미지 필드는 미리 생성된 ImageEntity 객체로 구성 (url 포함)
        val imageEntity1 = ImageEntity.from(
            Image(
                imageId = 0L,
                userId = 123L,
                fileName = "image1.jpg",
                url = "http://example.com/image1.jpg",
            )
        ).apply { this.product = null }
        val imageEntity2 = ImageEntity.from(
            Image(
                imageId = 0L,
                userId = 123L,
                fileName = "image2.jpg",
                url = "http://example.com/image2.jpg",
            )
        ).apply { this.product = null }

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
            // 엔티티에서는 shootingTime이 java.time.Duration 타입이므로, 도메인의 Duration을 toJavaDuration()으로 변환하여 할당
            shootingTime = shootingDuration.toJavaDuration(),
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

        // DB 저장 전이므로 createdDate와 updateDate는 null일 수 있음
        productEntity.createdDate shouldBe null
        productEntity.updateDate shouldBe null

        val product = productEntity.toDomain()
        product.productId shouldBe productEntity.productId
        product.userId shouldBe productEntity.userId
        product.title shouldBe productEntity.title
        product.description shouldBe productEntity.description
        product.price shouldBe productEntity.price
        product.typeCode shouldBe productEntity.typeCode

        product.shootingTime shouldBe productEntity.shootingTime?.toKotlinDuration()
        product.shootingLocation shouldBe productEntity.shootingLocation
        product.numberOfCostumes shouldBe productEntity.numberOfCostumes
        product.partnerShops.map { it.name } shouldContainExactlyInAnyOrder partnerShops.map { it.name }
        product.partnerShops.map { it.link } shouldContainExactlyInAnyOrder partnerShops.map { it.link }
        product.detailedInfo shouldBe productEntity.detailedInfo
        product.warrantyInfo shouldBe productEntity.warrantyInfo
        product.contactInfo shouldBe productEntity.contactInfo

        // 새로운 필드들에 대한 검증
        product.concept shouldBe productEntity.concept
        product.originalProvideType shouldBe productEntity.originalProvideType
        product.partialOriginalCount shouldBe productEntity.partialOriginalCount
        product.seasonYear shouldBe productEntity.seasonYear
        product.seasonHalf shouldBe productEntity.seasonHalf

        // 엔티티의 BaseTime 필드가 null이므로, 도메인의 createdAt과 updatedAt도 null이어야 함
        product.createdAt shouldBe null
        product.updatedAt shouldBe null

        // toDomain() 호출 시, images는 ImageEntity의 fileName을 추출하여 도메인 Product의 images 리스트로 변환됨
        product.images.map { it.fileName } shouldContainExactlyInAnyOrder productEntity.images.map { it.fileName }
    }
})
