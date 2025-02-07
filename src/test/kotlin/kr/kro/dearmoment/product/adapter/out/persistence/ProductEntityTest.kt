package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import java.time.LocalDateTime

class ProductEntityTest : StringSpec({

    "ProductEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val partnerShops =
            listOf(
                PartnerShop("상점1", "http://shop1.com"),
                PartnerShop("상점2", "http://shop2.com"),
            )

        // FULL 제공인 경우: partialOriginalCount는 null
        val product =
            Product(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = fixedDateTime,
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
                images = listOf("image1.jpg", "image2.jpg"),
            )

        val productEntity = ProductEntity.fromDomain(product)
        productEntity.productId shouldBe product.productId
        productEntity.userId shouldBe product.userId
        productEntity.title shouldBe product.title
        productEntity.description shouldBe product.description
        productEntity.price shouldBe product.price
        productEntity.typeCode shouldBe product.typeCode
        productEntity.shootingTime shouldBe product.shootingTime
        productEntity.shootingLocation shouldBe product.shootingLocation
        productEntity.numberOfCostumes shouldBe product.numberOfCostumes
        productEntity.partnerShops.map { it.name } shouldContainExactly partnerShops.map { it.name }
        productEntity.partnerShops.map { it.link } shouldContainExactly partnerShops.map { it.link }
        productEntity.detailedInfo shouldBe product.detailedInfo
        productEntity.warrantyInfo shouldBe product.warrantyInfo
        productEntity.contactInfo shouldBe product.contactInfo

        // 새로운 필드들에 대한 검증
        productEntity.concept shouldBe product.concept
        productEntity.originalProvideType shouldBe product.originalProvideType
        productEntity.partialOriginalCount shouldBe product.partialOriginalCount
        productEntity.seasonYear shouldBe product.seasonYear
        productEntity.seasonHalf shouldBe product.seasonHalf

        // Auditable 필드 비교
        productEntity.createdDate shouldBe product.createdAt
        productEntity.updateDate shouldBe product.updatedAt

        // 이미지 매핑 검증: ProductEntity의 images는 ImageEntity 리스트이므로, fileName만 추출하여 도메인 Product의 images(String 리스트)와 비교
        productEntity.images.map { it.fileName } shouldContainExactly product.images
    }

    "ProductEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        // PartnerShopEmbeddable는 엔티티 변환 시 사용한 값 타입으로, 테스트에서도 동일한 객체를 사용합니다.
        val partnerShops =
            listOf(
                PartnerShopEmbeddable("상점1", "http://shop1.com"),
                PartnerShopEmbeddable("상점2", "http://shop2.com"),
            )

        // PARTIAL 제공인 경우: partialOriginalCount는 3
        // 이미지 필드는 ImageEntity 객체로 생성
        val imageEntity1 =
            ImageEntity.from(
                Image(
                    imageId = 0L,
                    userId = 123L,
                    fileName = "image1.jpg",
                ),
            )
        val imageEntity2 =
            ImageEntity.from(
                Image(
                    imageId = 0L,
                    userId = 123L,
                    fileName = "image2.jpg",
                ),
            )
        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.PARTIAL,
                partialOriginalCount = 3,
                shootingTime = fixedDateTime,
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
        product.shootingTime shouldBe productEntity.shootingTime
        product.shootingLocation shouldBe productEntity.shootingLocation
        product.numberOfCostumes shouldBe productEntity.numberOfCostumes
        product.partnerShops.map { it.name } shouldContainExactly partnerShops.map { it.name }
        product.partnerShops.map { it.link } shouldContainExactly partnerShops.map { it.link }
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

        // toDomain() 호출 시, images는 ImageEntity의 fileName을 추출하여 String 리스트로 변환됩니다.
        product.images shouldContainExactly productEntity.images.map { it.fileName }
    }
})
