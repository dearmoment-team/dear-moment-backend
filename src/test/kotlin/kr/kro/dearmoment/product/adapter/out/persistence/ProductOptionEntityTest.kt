package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import java.lang.reflect.Field
import java.time.LocalDateTime

// 클래스 계층 전체에서 필드를 찾는 유틸리티 함수
private fun getFieldFromHierarchy(
    clazz: Class<*>,
    fieldName: String,
): Field {
    var current: Class<*>? = clazz
    while (current != null) {
        try {
            val field = current.getDeclaredField(fieldName)
            field.isAccessible = true
            return field
        } catch (e: NoSuchFieldException) {
            current = current.superclass
        }
    }
    throw NoSuchFieldException("Field $fieldName not found in class hierarchy of ${clazz.name}")
}

// Auditable 클래스에 정의된 필드명 "createdDate", "updateDate"를 사용하여 auditing 필드를 리플렉션으로 설정합니다.
private fun setAuditing(
    entity: ProductOptionEntity,
    created: LocalDateTime,
    updated: LocalDateTime,
) {
    val createdDateField: Field = getFieldFromHierarchy(ProductOptionEntity::class.java, "createdDate")
    createdDateField.set(entity, created)

    val updateDateField: Field = getFieldFromHierarchy(ProductOptionEntity::class.java, "updateDate")
    updateDateField.set(entity, updated)
}

class ProductOptionEntityTest : StringSpec({

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
        optionEntity.product.productId shouldBe 1L
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

        // fromDomain()에서 auditing 필드를 복사하지 않으므로 null이어야 합니다.
        optionEntity.createdDate shouldBe null
        optionEntity.updateDate shouldBe null
    }

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

        // 직접 ProductOptionEntity 인스턴스를 생성
        val optionEntity =
            ProductOptionEntity(
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
                version = 0L,
            )

        // auditing 필드 설정 (Auditable의 "createdDate", "updateDate" 사용)
        setAuditing(optionEntity, fixedCreatedAt, fixedUpdatedAt)

        // when
        val packageOption = optionEntity.toDomain()

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
            listOf(PartnerShopCategory.HAIR_MAKEUP, PartnerShopCategory.DRESS)

        packageOption.createdAt shouldBe fixedCreatedAt
        packageOption.updatedAt shouldBe fixedUpdatedAt
    }
})
