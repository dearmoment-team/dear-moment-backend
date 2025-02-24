package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PackageCategory
import kr.kro.dearmoment.product.domain.model.ProductOption

import java.time.LocalDateTime

class ProductOptionEntityTest : StringSpec({

    /**
     * ProductOptionEntity.fromDomain(...) 테스트
     * - 도메인 ProductOption -> ProductOptionEntity 변환 검증
     */
    "ProductOptionEntity는 도메인 모델에서 올바르게 변환되어야 한다 (단품 옵션 예시)" {
        // given
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 123L,
            title = "샘플 상품",
            basePrice = 1000L,
        )

        val fixedCreatedAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val singleOption = ProductOption(
            optionId = 1L,
            productId = 1L,
            name = "단품 옵션",
            optionType = OptionType.SINGLE,
            additionalPrice = 500L,
            description = "단품 옵션 설명",
            costumeCount = 2,
            shootingLocation = "스튜디오 A",
            shootingMinutes = 60,
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
        optionEntity.additionalPrice shouldBe 500L
        optionEntity.description shouldBe "단품 옵션 설명"
        optionEntity.costumeCount shouldBe 2
        optionEntity.shootingLocation shouldBe "스튜디오 A"
        optionEntity.shootingMinutes shouldBe 60

        // Auditing 필드는 신규 엔티티 생성 시 null로 유지 (등록 시점에 자동 세팅)
        optionEntity.createdAt shouldBe null
        optionEntity.updatedAt shouldBe null
    }

    /**
     * ProductOptionEntity.toDomain(...) 테스트
     * - ProductOptionEntity -> 도메인 ProductOption 변환 검증
     */
    "ProductOptionEntity는 도메인 모델로 올바르게 변환되어야 한다 (패키지 옵션 예시)" {
        // given
        val productEntity = ProductEntity(
            productId = 2L,
            userId = 456L,
            title = "샘플 상품2",
            basePrice = 2000L,
        )

        val partnerShopsEmbeddable = listOf(
            PartnerShopEmbeddable(name = "헤어메이크업1", link = "http://hm1.com"),
            PartnerShopEmbeddable(name = "드레스샵A", link = "http://dressA.com")
        )

        val packageOptionEntity = ProductOptionEntity(
            optionId = 10L,
            product = productEntity,
            name = "패키지 옵션",
            optionType = OptionType.PACKAGE,
            additionalPrice = 1500L,
            description = "패키지 옵션 설명",
            costumeCount = 0,  // 패키지이므로 단품 필드는 의미 없음
            shootingLocation = "",
            shootingMinutes = 0,
            packageCategory = PackageCategory.DRESS,
            partnerShops = partnerShopsEmbeddable,
            createdAt = LocalDateTime.of(2023, 2, 2, 10, 0, 0),
            updatedAt = LocalDateTime.of(2023, 2, 2, 12, 0, 0)
        )

        // when
        val packageOption = packageOptionEntity.toDomain()

        // then
        packageOption.optionId shouldBe 10L
        packageOption.productId shouldBe 2L
        packageOption.name shouldBe "패키지 옵션"
        packageOption.optionType shouldBe OptionType.PACKAGE
        packageOption.additionalPrice shouldBe 1500L
        packageOption.description shouldBe "패키지 옵션 설명"

        // 패키지 필드 확인
        packageOption.packageCategory shouldBe PackageCategory.DRESS
        packageOption.partnerShops.size shouldBe 2
        packageOption.partnerShops.map { it.name } shouldContainExactly listOf("헤어메이크업1", "드레스샵A")
        packageOption.partnerShops.map { it.link } shouldContainExactly listOf("http://hm1.com", "http://dressA.com")

        // Auditing 필드는 이미 세팅된 값이므로 그대로 노출
        packageOption.createdAt shouldBe LocalDateTime.of(2023, 2, 2, 10, 0, 0)
        packageOption.updatedAt shouldBe LocalDateTime.of(2023, 2, 2, 12, 0, 0)
    }
})
