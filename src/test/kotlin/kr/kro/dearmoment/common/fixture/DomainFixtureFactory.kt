package kr.kro.dearmoment.common.fixture

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.StudioLike
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.studio.domain.Studio

fun studioFixture(
    id: Long = 1L,
    userId: Long = 1L,
): Studio =
    fixtureBuilder.giveMeKotlinBuilder<Studio>()
        .setExp(Studio::id, id)
        .setExp(Studio::userId, userId)
        .setExp(Studio::products, listOf(productFixture()))
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
        .setExp(Product::retouchStyles, setOf(RetouchStyle.MODERN))
        .setExp(Product::additionalImages, listOf("url1", "url2", "url3", "url4"))
        .setExp(Product::options, listOf(productOptionFixture()))
        .setExp(Product::subImages, listOf("url1", "url2", "url3", "url4"))
        .setExp(Product::productId, 1L)
        .sample()

fun productOptionFixture(): ProductOption =
    fixtureBuilder.giveMeKotlinBuilder<ProductOption>()
        .setExp(ProductOption::discountPrice, 800_000)
        .setExp(ProductOption::originalPrice, 1_000_000)
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

fun studioLikeFixture(userId: Long): StudioLike =
    fixtureBuilder.giveMeKotlinBuilder<StudioLike>()
        .setExp(StudioLike::userId, userId)
        .setExp(StudioLike::studio, studioFixture())
        .sample()

fun productOptionLikeFixture(userId: Long): ProductOptionLike =
    fixtureBuilder.giveMeKotlinBuilder<ProductOptionLike>()
        .setExp(ProductOptionLike::userId, userId)
        .setExp(ProductOptionLike::product, productFixture())
        .setExp(ProductOptionLike::productOptionId, 1L)
        .sample()

fun productOptionInquiryFixture(userId: Long): ProductOptionInquiry =
    fixtureBuilder.giveMeKotlinBuilder<ProductOptionInquiry>()
        .setExp(ProductOptionInquiry::userId, userId)
        .sample()
