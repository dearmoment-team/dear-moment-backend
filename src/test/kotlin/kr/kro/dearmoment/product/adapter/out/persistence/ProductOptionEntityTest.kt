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
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionEntityTest : StringSpec({

    "ProductOptionEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        // PartnerShopEmbeddable는 값 타입(Embeddable)으로 제품 엔티티에서 사용됩니다.
        val partnerShops =
            listOf(
                PartnerShopEmbeddable("상점1", "http://shop1.com"),
                PartnerShopEmbeddable("상점2", "http://shop2.com"),
            )

        // 도메인 Product 객체를 생성 (images는 String 리스트로 구성)
        val productDomain =
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
                shootingTime = null,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                seasonYear = null,
                seasonHalf = null,
                partnerShops =
                    listOf(
                        PartnerShop("상점1", "http://shop1.com"),
                        PartnerShop("상점2", "http://shop2.com"),
                    ),
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
                images = listOf("image1.jpg", "image2.jpg"),
            )

        // fromDomain() 메서드를 사용하여 ProductEntity 생성 시,
        // 내부에서 이미지 String 리스트가 ImageEntity로 변환되고, 각 ImageEntity에 product 연관관계가 설정됩니다.
        val productEntity = ProductEntity.fromDomain(productDomain)
        productEntity.productId shouldBe productDomain.productId
        productEntity.userId shouldBe productDomain.userId
        productEntity.title shouldBe productDomain.title
        productEntity.description shouldBe productDomain.description
        productEntity.price shouldBe productDomain.price
        productEntity.typeCode shouldBe productDomain.typeCode
        productEntity.shootingTime shouldBe productDomain.shootingTime
        productEntity.shootingLocation shouldBe productDomain.shootingLocation
        productEntity.numberOfCostumes shouldBe productDomain.numberOfCostumes
        productEntity.partnerShops.map { it.name } shouldContainExactly partnerShops.map { it.name }
        productEntity.partnerShops.map { it.link } shouldContainExactly partnerShops.map { it.link }
        productEntity.detailedInfo shouldBe productDomain.detailedInfo
        productEntity.warrantyInfo shouldBe productDomain.warrantyInfo
        productEntity.contactInfo shouldBe productDomain.contactInfo

        // 새로운 필드들에 대한 검증
        productEntity.concept shouldBe productDomain.concept
        productEntity.originalProvideType shouldBe productDomain.originalProvideType
        productEntity.partialOriginalCount shouldBe productDomain.partialOriginalCount
        // Auditable 필드 비교 (여기서는 null로 설정)
        productEntity.createdDate shouldBe productDomain.createdAt
        productEntity.updateDate shouldBe productDomain.updatedAt

        // 이미지 매핑 검증: ProductEntity의 images는 ImageEntity 리스트이므로, 각 ImageEntity의 fileName을 추출하여
        // 도메인 Product의 images(String 리스트)와 일치하는지 확인합니다.
        productEntity.images.map { it.fileName } shouldContainExactly productDomain.images
    }

    "ProductOptionEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        val partnerShops =
            listOf(
                PartnerShopEmbeddable("상점1", "http://shop1.com"),
                PartnerShopEmbeddable("상점2", "http://shop2.com"),
            )

        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)

        // PARTIAL 제공인 경우: partialOriginalCount는 3
        // 이미지 필드는 미리 생성된 ImageEntity 객체로 구성합니다.
        val imageEntity1 =
            ImageEntity.from(
                Image(
                    imageId = 0L,
                    userId = 123L,
                    fileName = "image1.jpg",
                ),
            ).apply { this.product = null } // 이후 product 연관관계를 설정할 예정
        val imageEntity2 =
            ImageEntity.from(
                Image(
                    imageId = 0L,
                    userId = 123L,
                    fileName = "image2.jpg",
                ),
            ).apply { this.product = null }

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

        val productOptionEntity =
            ProductOptionEntity.fromDomain(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "테스트 옵션",
                    additionalPrice = 500L,
                    description = "옵션 설명",
                    createdAt = null,
                    updatedAt = null,
                ),
                productEntity,
            )
        productOptionEntity.optionId shouldBe 1L
        productOptionEntity.name shouldBe "테스트 옵션"
        productOptionEntity.additionalPrice shouldBe 500L
        productOptionEntity.description shouldBe "옵션 설명"
        productOptionEntity.product?.productId shouldBe 1L

        // fromDomain()에서 createdDate 및 updateDate는 그대로 null이어야 합니다.
        productOptionEntity.createdDate shouldBe null
        productOptionEntity.updateDate shouldBe null

        // toDomain() 테스트: 엔티티에서 도메인으로 변환 시 값들이 올바르게 전달되는지 확인합니다.
        val productOption = productOptionEntity.toDomain()
        productOption.optionId shouldBe productOptionEntity.optionId
        productOption.name shouldBe productOptionEntity.name
        productOption.additionalPrice shouldBe productOptionEntity.additionalPrice
        productOption.description shouldBe productOptionEntity.description
        productOption.productId shouldBe productOptionEntity.product?.productId
        productOption.createdAt shouldBe productOptionEntity.createdDate
        productOption.updatedAt shouldBe productOptionEntity.updateDate
    }
})
