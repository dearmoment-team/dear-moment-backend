package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.PartnerShopEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioPartnerShopEmbeddable
import kr.kro.dearmoment.studio.domain.StudioPartnerShopCategory

fun studioEntityFixture(userId: Long = 12345L): StudioEntity =
    fixtureBuilder.giveMeKotlinBuilder<StudioEntity>()
        .setExp(StudioEntity::id, 0L)
        .setExp(StudioEntity::name, "스튜디오 디어모먼트")
        .setExp(StudioEntity::studioIntro, "소개글")
        .setExp(StudioEntity::artistsIntro, "스튜디오 A 작가 입니다.")
        .setExp(StudioEntity::userId, userId)
        .setExp(StudioEntity::status, "ACTIVE")
        .setExp(StudioEntity::partnerShops, setOf(studioPartnerShopEmbeddableFixture()))
        .sample()

fun studioPartnerShopEmbeddableFixture(): StudioPartnerShopEmbeddable =
    fixtureBuilder.giveMeKotlinBuilder<StudioPartnerShopEmbeddable>()
        .setExp(StudioPartnerShopEmbeddable::category, StudioPartnerShopCategory.HAIR_MAKEUP.name)
        .setExp(StudioPartnerShopEmbeddable::name, "Test Shop")
        .setExp(StudioPartnerShopEmbeddable::urlLink, "http://testshop.com")
        .sample()

fun productEntityFixture(
    userId: Long = 12345L,
    studioEntity: StudioEntity = studioEntityFixture(userId),
): ProductEntity =
    fixtureBuilder.giveMeKotlinBuilder<ProductEntity>()
        .setNull(ProductEntity::productId)
        .setNotNull(ProductEntity::shootingPlace)
        .setNotNull(ProductEntity::title)
        .setNotNull(ProductEntity::version)
        .setExp(ProductEntity::productType, ProductType.WEDDING_SNAP)
        .setExp(ProductEntity::userId, userId)
        .setExp(ProductEntity::options, mutableListOf<ProductOptionEntity>())
        .setExp(ProductEntity::studio, studioEntity)
        .setExp(ProductEntity::subImages, List(4) { imageEmbeddableFixture() })
        .setPostCondition { it.title.isNotBlank() }
        .setPostCondition { it.retouchStyles.size <= 2 }
        .setPostCondition { it.retouchStyles.size <= 2 }
        .sample()

fun imageEmbeddableFixture(): ImageEmbeddable =
    fixtureBuilder.giveMeKotlinBuilder<ImageEmbeddable>()
        .sample()

fun partnerShopEmbeddableFixture(): PartnerShopEmbeddable =
    fixtureBuilder.giveMeKotlinBuilder<PartnerShopEmbeddable>()
        .setExp(PartnerShopEmbeddable::category, PartnerShopCategory.HAIR_MAKEUP)
        .setExp(PartnerShopEmbeddable::name, "Test Shop")
        .setExp(PartnerShopEmbeddable::link, "http://testshop.com")
        .sample()

fun productOptionEntityFixture(productEntity: ProductEntity = productEntityFixture()): ProductOptionEntity {
    return fixtureBuilder.giveMeKotlinBuilder<ProductOptionEntity>()
        .setNull(ProductOptionEntity::optionId)
        .setExp(ProductOptionEntity::product, productEntity)
        .setExp(ProductOptionEntity::discountPrice, 100_000)
        .setExp(ProductOptionEntity::originalPrice, 1_000_000)
        .setExp(ProductOptionEntity::partnerShops, listOf(partnerShopEmbeddableFixture()))
        .setPostCondition { it.shootingHours > 0 }
        .setPostCondition { it.shootingMinutes > 0 }
        .setPostCondition { it.shootingLocationCount > 0 }
        .setPostCondition { it.costumeCount > 0 }
        .setPostCondition { it.retouchedCount > 0 }
        .sample()
}
