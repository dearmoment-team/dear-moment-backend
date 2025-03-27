package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.PartnerShopEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.OptionType
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioPartnerShopEmbeddable
import kr.kro.dearmoment.studio.domain.StudioPartnerShopCategory

fun studioEntityFixture(userId: Long = 12345L): StudioEntity =
    fixtureBuilder.giveMeKotlinBuilder<StudioEntity>()
        .setExp(StudioEntity::id, 0L)
        .setExp(StudioEntity::name, "스튜디오 디어모먼트")
        .setExp(StudioEntity::studioIntro, "소개글")
        .setExp(StudioEntity::artistsIntro, "스튜디오 A 작가 입니다.")
        .setExp(StudioEntity::contact, "010-1234-5678")
        .setExp(StudioEntity::userId, userId)
        .setExp(StudioEntity::status, "ACTIVE")
        .setExp(StudioEntity::instagramUrl, "instagram.com")
        .setExp(StudioEntity::kakaoChannelUrl, "kakaotalk.com")
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
    studioEntity: StudioEntity,
): ProductEntity =
    fixtureBuilder.giveMeKotlinBuilder<ProductEntity>()
        .setNull(ProductEntity::productId)
        .setNotNull(ProductEntity::title)
        .setNotNull(ProductEntity::version)
        .set(ProductEntity::productType, ProductType.entries.shuffled().first())
        .set(ProductEntity::shootingPlace, ShootingPlace.JEJU)
        .set(ProductEntity::userId, userId)
        .set(ProductEntity::options, mutableListOf<ProductOptionEntity>())
        .set(ProductEntity::studio, studioEntity)
        .set(ProductEntity::subImages, List(4) { imageEmbeddableFixture() })
        .set(ProductEntity::likeCount, (1..100).random().toLong())
        .set(ProductEntity::optionLikeCount, (1..100).random().toLong())
        .set(ProductEntity::inquiryCount, (1..100).random().toLong())
        .setPostCondition { it.title.isNotBlank() }
        .setPostCondition { it.availableSeasons.size <= ShootingSeason.entries.size }
        .sizeExp(ProductEntity::cameraTypes, 1)
        .sizeExp(ProductEntity::retouchStyles, 2)
        .sizeExp(ProductEntity::availableSeasons, 1, 4)
        .sample()

fun imageEmbeddableFixture(): ImageEmbeddable =
    fixtureBuilder.giveMeKotlinBuilder<ImageEmbeddable>()
        .sample()

fun partnerShopEmbeddableFixture(): PartnerShopEmbeddable =
    fixtureBuilder.giveMeKotlinBuilder<PartnerShopEmbeddable>()
        .setExp(PartnerShopEmbeddable::category, PartnerShopCategory.entries.shuffled().take(1)[0])
        .setExp(PartnerShopEmbeddable::name, "Test Shop")
        .setExp(PartnerShopEmbeddable::link, "http://testshop.com")
        .sample()

fun productOptionEntityFixture(productEntity: ProductEntity): ProductOptionEntity {
    return fixtureBuilder.giveMeKotlinBuilder<ProductOptionEntity>()
        .setNull(ProductOptionEntity::optionId)
        .set(ProductOptionEntity::product, productEntity)
        .set(ProductOptionEntity::discountPrice, (50_000L..150_000L).random())
        .set(ProductOptionEntity::originalPrice, (150_000L..300_000L).random())
        .set(ProductOptionEntity::optionType, OptionType.SINGLE)
        .set(ProductOptionEntity::shootingHours, 1)
        .set(ProductOptionEntity::shootingMinutes, 1)
        .set(
            ProductOptionEntity::partnerShops,
            listOf(partnerShopEmbeddableFixture(), partnerShopEmbeddableFixture()),
        )
        .setPostCondition {
            it.name.isNotBlank() &&
                it.shootingHours > 0 &&
                it.shootingMinutes > 0 &&
                it.shootingLocationCount > 0 &&
                it.costumeCount > 0 &&
                it.partnerShops.size <= PartnerShopCategory.entries.size &&
                it.retouchedCount > 0
        }
        .sample()
}

fun studioInquiryEntityFixture(userId: Long = 1L) =
    fixtureBuilder.giveMeKotlinBuilder<StudioInquiryEntity>()
        .setExp(StudioInquiryEntity::id, 0)
        .setExp(StudioInquiryEntity::userId, userId)
        .setPostCondition { it.title.isNotBlank() }
        .setPostCondition { it.content.isNotBlank() }
        .sample()

fun productOptionInquiryEntityFixture(
    userId: Long = 1L,
    option: ProductOptionEntity = productOptionEntityFixture(productEntityFixture(studioEntity = studioEntityFixture())),
) = fixtureBuilder.giveMeKotlinBuilder<ProductOptionInquiryEntity>()
    .setExp(ProductOptionInquiryEntity::id, 0)
    .setExp(ProductOptionInquiryEntity::userId, userId)
    .setExp(ProductOptionInquiryEntity::option, option)
    .sample()
