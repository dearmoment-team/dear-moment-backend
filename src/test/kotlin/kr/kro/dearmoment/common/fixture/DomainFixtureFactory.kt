package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.OptionType
import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import kr.kro.dearmoment.studio.domain.Studio
import java.util.UUID

fun studioFixture(
    id: Long = 0L,
    userId: UUID = UUID.randomUUID(),
): Studio =
    fixtureBuilder.giveMeKotlinBuilder<Studio>()
        .setExp(Studio::id, id)
        .setExp(Studio::userId, userId)
        .setExp(Studio::name, "스튜디오 A")
        .setExp(Studio::contact, "010-1234-5678")
        .setExp(Studio::studioIntro, "소개글")
        .setExp(Studio::artistsIntro, "스튜디오 A 작가 입니다.")
        .setExp(Studio::instagramUrl, "instagramUrl")
        .setExp(Studio::kakaoChannelUrl, "kakaotalkUrl")
        .sample()

fun productFixture(): Product =
    fixtureBuilder.giveMeKotlinBuilder<Product>()
        .setNotNull(Product::productId)
        .setExp(Product::title, "상품 이롬")
        .set(Product::cameraTypes, setOf(CameraType.DIGITAL))
        .set(Product::availableSeasons, ShootingSeason.entries.shuffled().take(2).map { it }.toSet())
        .setExp(Product::retouchStyles, RetouchStyle.entries.shuffled().take(2).map { it }.toSet())
        .setExp(Product::additionalImages, listOf("url1", "url2", "url3", "url4"))
        .setExp(Product::options, listOf(productOptionFixture()))
        .setExp(Product::subImages, listOf("url1", "url2", "url3", "url4"))
        .set(Product::likeCount, (1..100).random().toLong())
        .set(Product::optionLikeCount, (1..100).random().toLong())
        .set(Product::inquiryCount, (1..100).random().toLong())
        .setExp(Product::productId, 1L)
        .setExp(Product::studio, studioFixture())
        .sample()

fun productOptionFixture(): ProductOption =
    fixtureBuilder.giveMeKotlinBuilder<ProductOption>()
        .setExp(ProductOption::discountPrice, (50_000L..150_000L).random())
        .setExp(ProductOption::originalPrice, (150_000L..300_000L).random())
        .setExp(ProductOption::optionType, OptionType.PACKAGE)
        .setExp(
            ProductOption::partnerShops,
            listOf(
                PartnerShop(
                    category = PartnerShopCategory.DRESS,
                    name = "Dress Partner SHop",
                    link = "url.link.com",
                ),
            ),
        )
        .setExp(ProductOption::optionId, 1L)
        .setExp(ProductOption::name, "Basic")
        .sample()

fun productLikeFixture(userId: UUID): ProductLike =
    fixtureBuilder.giveMeKotlinBuilder<ProductLike>()
        .setExp(ProductLike::userId, userId)
        .setExp(ProductLike::product, productFixture())
        .sample()

fun productOptionLikeFixture(userId: UUID): ProductOptionLike =
    fixtureBuilder.giveMeKotlinBuilder<ProductOptionLike>()
        .setExp(ProductOptionLike::userId, userId)
        .setExp(ProductOptionLike::product, productFixture())
        .setExp(ProductOptionLike::productOptionId, 1L)
        .sample()

fun productOptionInquiryFixture(userId: UUID): ProductOptionInquiry =
    fixtureBuilder.giveMeKotlinBuilder<ProductOptionInquiry>()
        .setExp(ProductOptionInquiry::userId, userId)
        .sample()
