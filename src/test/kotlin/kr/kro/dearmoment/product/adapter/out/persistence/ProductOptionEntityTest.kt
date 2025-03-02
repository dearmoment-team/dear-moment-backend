package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import java.time.LocalDateTime

// 파일 최상위에 테스트용 서브클래스를 선언하여, auditing 필드에 접근할 수 있도록 합니다.
private class TestableProductOptionEntity(
    optionId: Long? = null,
    product: ProductEntity? = null,
    name: String = "",
    optionType: OptionType = OptionType.SINGLE,
    discountAvailable: Boolean = false,
    originalPrice: Long = 0L,
    discountPrice: Long = 0L,
    description: String? = null,
    costumeCount: Int = 0,
    shootingLocationCount: Int = 0,
    shootingHours: Int = 0,
    shootingMinutes: Int = 0,
    retouchedCount: Int = 0,
    partnerShops: List<PartnerShopEmbeddable> = emptyList(),
    version: Long = 0L,
) : ProductOptionEntity(
    optionId = optionId,
    product = product,
    name = name,
    optionType = optionType,
    discountAvailable = discountAvailable,
    originalPrice = originalPrice,
    discountPrice = discountPrice,
    description = description,
    costumeCount = costumeCount,
    shootingLocationCount = shootingLocationCount,
    shootingHours = shootingHours,
    shootingMinutes = shootingMinutes,
    retouchedCount = retouchedCount,
    partnerShops = partnerShops,
    version = version,
) {
    // 테스트용으로 auditing 필드에 값을 주입할 수 있도록 합니다.
    fun setAuditing(
        created: LocalDateTime,
        updated: LocalDateTime,
    ) {
        this.createdDate = created
        this.updateDate = updated
    }
}

class ProductOptionEntityTest : StringSpec({

    /**
     * 단품 옵션 도메인 모델에서 엔티티로 변환할 때 Auditing 필드는 복사되지 않으므로,
     * 해당 필드가 null임을 검증합니다.
     */
    "ProductOptionEntity는 단품 옵션 도메인 모델에서 올바르게 변환되어야 한다" {
        // given
        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                productType = ProductType.WEDDING_SNAP,
                shootingPlace = ShootingPlace.JEJU,
                title = "샘플 상품",
            )

        val fixedCreatedAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val singleOption =
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "단품 옵션",
                optionType = OptionType.SINGLE,
                discountAvailable = false,
                originalPrice = 500L,
                discountPrice = 0L,
                description = "단품 옵션 설명",
                costumeCount = 2,
                shootingLocationCount = 1,
                shootingHours = 0,
                shootingMinutes = 60,
                retouchedCount = 1,
                partnerShops = emptyList(),
                createdAt = fixedCreatedAt,
                updatedAt = fixedUpdatedAt,
            )

        // when
        val optionEntity = ProductOptionEntity.fromDomain(singleOption, productEntity)

        // then
        optionEntity.optionId shouldBe 1L
        optionEntity.product?.productId shouldBe 1L
        optionEntity.name shouldBe "단품 옵션"
        optionEntity.optionType shouldBe OptionType.SINGLE
        optionEntity.originalPrice shouldBe 500L
        optionEntity.discountAvailable shouldBe false
        optionEntity.discountPrice shouldBe 0L
        optionEntity.description shouldBe "단품 옵션 설명"
        optionEntity.costumeCount shouldBe 2
        optionEntity.shootingLocationCount shouldBe 1
        optionEntity.shootingHours shouldBe 0
        optionEntity.shootingMinutes shouldBe 60
        optionEntity.retouchedCount shouldBe 1

        // Auditing 필드는 fromDomain()에서 복사되지 않으므로 null이어야 함
        optionEntity.createdDate shouldBe null
        optionEntity.updateDate shouldBe null
    }

    /**
     * 패키지 옵션 엔티티를 도메인 모델로 변환할 때, Auditing 필드가 올바르게 전달되는지 검증합니다.
     * 테스트를 위해 auditing 필드를 수동으로 설정할 수 있는 TestableProductOptionEntity를 사용합니다.
     */
    "ProductOptionEntity는 패키지 옵션 도메인 모델로 올바르게 변환되어야 한다" {
        // given
        val productEntity =
            ProductEntity(
                productId = 2L,
                userId = 456L,
                productType = ProductType.WEDDING_SNAP,
                shootingPlace = ShootingPlace.JEJU,
                title = "샘플 상품2",
            )

        val partnerShopsEmbeddable =
            listOf(
                PartnerShopEmbeddable(
                    category = PartnerShopCategory.HAIR_MAKEUP,
                    name = "헤어메이크업1",
                    link = "http://hm1.com",
                ),
                PartnerShopEmbeddable(
                    category = PartnerShopCategory.DRESS,
                    name = "드레스샵A",
                    link = "http://dressA.com",
                ),
            )

        val fixedCreatedAt = LocalDateTime.of(2023, 2, 2, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 2, 2, 12, 0, 0)

        val testableOptionEntity =
            TestableProductOptionEntity(
                optionId = 10L,
                product = productEntity,
                name = "패키지 옵션",
                optionType = OptionType.PACKAGE,
                discountAvailable = false,
                originalPrice = 1500L,
                discountPrice = 0L,
                description = "패키지 옵션 설명",
                costumeCount = 0,
                shootingLocationCount = 0,
                shootingHours = 0,
                shootingMinutes = 0,
                retouchedCount = 0,
                partnerShops = partnerShopsEmbeddable,
            ).apply {
                setAuditing(fixedCreatedAt, fixedUpdatedAt)
            }

        // when
        val packageOption = testableOptionEntity.toDomain()

        // then
        packageOption.optionId shouldBe 10L
        packageOption.productId shouldBe 2L
        packageOption.name shouldBe "패키지 옵션"
        packageOption.optionType shouldBe OptionType.PACKAGE
        packageOption.originalPrice shouldBe 1500L
        packageOption.discountAvailable shouldBe false
        packageOption.discountPrice shouldBe 0L
        packageOption.description shouldBe "패키지 옵션 설명"
        packageOption.costumeCount shouldBe 0
        packageOption.shootingLocationCount shouldBe 0
        packageOption.shootingHours shouldBe 0
        packageOption.shootingMinutes shouldBe 0
        packageOption.retouchedCount shouldBe 0

        packageOption.partnerShops.size shouldBe 2
        packageOption.partnerShops.map { it.name } shouldContainExactly listOf("헤어메이크업1", "드레스샵A")
        packageOption.partnerShops.map { it.link } shouldContainExactly listOf("http://hm1.com", "http://dressA.com")
        packageOption.partnerShops.map { it.category } shouldContainExactly
                listOf(
                    PartnerShopCategory.HAIR_MAKEUP,
                    PartnerShopCategory.DRESS,
                )

        packageOption.createdAt shouldBe fixedCreatedAt
        packageOption.updatedAt shouldBe fixedUpdatedAt
    }
})
