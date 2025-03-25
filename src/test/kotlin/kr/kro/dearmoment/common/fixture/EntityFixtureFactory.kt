package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import kr.kro.dearmoment.inquiry.adapter.output.persistence.product.ProductOptionInquiryEntity
import kr.kro.dearmoment.inquiry.adapter.output.persistence.studio.StudioInquiryEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.PartnerShopEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
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
        .setExp(ProductEntity::cameraTypes, mutableSetOf(CameraType.DIGITAL))
        .setExp(ProductEntity::availableSeasons, ShootingSeason.entries.shuffled().take(2).map { it }.toMutableSet())
        .setExp(ProductEntity::productType, ProductType.entries.shuffled().take(1)[0])
        .setExp(ProductEntity::shootingPlace, ShootingPlace.JEJU)
        .setExp(ProductEntity::userId, userId)
        .setExp(ProductEntity::options, mutableListOf<ProductOptionEntity>())
        .setExp(ProductEntity::studio, studioEntity)
        .setExp(ProductEntity::subImages, List(4) { imageEmbeddableFixture() })
        .setExp(ProductEntity::likeCount, (1..100).random())
        .setExp(ProductEntity::inquiryCount, (1..100).random())
        .setPostCondition { it.title.isNotBlank() }
        .setExp(ProductEntity::retouchStyles, RetouchStyle.entries.shuffled().take(2).map { it }.toMutableSet())
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
        .setExp(ProductOptionEntity::product, productEntity)
        .setExp(ProductOptionEntity::discountPrice, (50_000L..150_000L).random())
        .setExp(ProductOptionEntity::originalPrice, (150_000L..300_000L).random())
        .setExp(ProductOptionEntity::optionType, OptionType.SINGLE)
        .setExp(ProductOptionEntity::likeCount, (1..100).random())
        .setExp(
            ProductOptionEntity::partnerShops,
            listOf(partnerShopEmbeddableFixture(), partnerShopEmbeddableFixture()),
        )
        .setPostCondition { it.name.isNotBlank() }
        .setPostCondition { it.shootingHours > 0 }
        .setPostCondition { it.shootingMinutes > 0 }
        .setPostCondition { it.shootingLocationCount > 0 }
        .setPostCondition { it.costumeCount > 0 }
        .setPostCondition { it.retouchedCount > 0 }
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
