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

class ProductOptionEntityTest : StringSpec({

    /**
     * ProductOptionEntity.fromDomain(...) 테스트 (단품 옵션 예시)
     * - 도메인 ProductOption -> ProductOptionEntity 변환 검증
     */
    "ProductOptionEntity는 단품 옵션 도메인 모델에서 올바르게 변환되어야 한다" {
        // given
        // 단품 옵션에서는 촬영 장소 문자열 대신 촬영 장소 수, 촬영 시간(시/분), 보정본 수 등을 사용
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 123L,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "샘플 상품"
        )

        val fixedCreatedAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val singleOption = ProductOption(
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
            updatedAt = fixedUpdatedAt
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

        // 기존 도메인의 createdAt/updatedAt 값을 그대로 복사하므로, 검증 값도 고정된 값과 일치해야 함
        optionEntity.createdAt shouldBe fixedCreatedAt
        optionEntity.updatedAt shouldBe fixedUpdatedAt
    }

    /**
     * ProductOptionEntity.toDomain(...) 테스트 (패키지 옵션 예시)
     * - ProductOptionEntity -> 도메인 ProductOption 변환 검증
     */
    "ProductOptionEntity는 패키지 옵션 도메인 모델로 올바르게 변환되어야 한다" {
        // given
        val productEntity = ProductEntity(
            productId = 2L,
            userId = 456L,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "샘플 상품2"
        )

        // 기존 packageCategory 필드는 제거되고, 대신 각 파트너샵에 category 지정
        val partnerShopsEmbeddable = listOf(
            PartnerShopEmbeddable(
                category = PartnerShopCategory.HAIR_MAKEUP,
                name = "헤어메이크업1",
                link = "http://hm1.com"
            ),
            PartnerShopEmbeddable(
                category = PartnerShopCategory.DRESS,
                name = "드레스샵A",
                link = "http://dressA.com"
            )
        )

        val fixedCreatedAt = LocalDateTime.of(2023, 2, 2, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 2, 2, 12, 0, 0)

        val packageOptionEntity = ProductOptionEntity(
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
            // 패키지 옵션은 partnerShops가 필수
            partnerShops = partnerShopsEmbeddable,
            createdAt = fixedCreatedAt,
            updatedAt = fixedUpdatedAt
        )

        // when
        val packageOption = packageOptionEntity.toDomain()

        // then
        packageOption.optionId shouldBe 10L
        packageOption.productId shouldBe 2L
        packageOption.name shouldBe "패키지 옵션"
        packageOption.optionType shouldBe OptionType.PACKAGE
        packageOption.originalPrice shouldBe 1500L
        packageOption.discountAvailable shouldBe false
        packageOption.discountPrice shouldBe 0L
        packageOption.description shouldBe "패키지 옵션 설명"

        // 단품용 필드는 사용하지 않으므로 0이어야 함
        packageOption.costumeCount shouldBe 0
        packageOption.shootingLocationCount shouldBe 0
        packageOption.shootingHours shouldBe 0
        packageOption.shootingMinutes shouldBe 0
        packageOption.retouchedCount shouldBe 0

        // partnerShops 확인 (각 PartnerShop에 category, name, link가 지정되어야 함)
        packageOption.partnerShops.size shouldBe 2
        packageOption.partnerShops.map { it.name } shouldContainExactly listOf("헤어메이크업1", "드레스샵A")
        packageOption.partnerShops.map { it.link } shouldContainExactly listOf("http://hm1.com", "http://dressA.com")
        packageOption.partnerShops.map { it.category } shouldContainExactly listOf(
            PartnerShopCategory.HAIR_MAKEUP,
            PartnerShopCategory.DRESS
        )

        // Auditing 필드는 도메인 값 그대로 복사되어야 함
        packageOption.createdAt shouldBe fixedCreatedAt
        packageOption.updatedAt shouldBe fixedUpdatedAt
    }
})
