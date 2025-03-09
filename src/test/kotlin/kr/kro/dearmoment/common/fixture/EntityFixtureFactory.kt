package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import kr.kro.dearmoment.product.adapter.out.persistence.PartnerShopEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity

fun studioEntityFixture(userId: Long = 12345L): StudioEntity =
    fixtureBuilder.giveMeKotlinBuilder<StudioEntity>()
        .setExp(StudioEntity::id, 0L)
        .setExp(StudioEntity::userId, userId)
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
        .setExp(ProductOptionEntity::partnerShops, listOf(partnerShopEmbeddableFixture()))
        .sample()
}
