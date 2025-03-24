package kr.kro.dearmoment.product.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.domain.model.option.OptionType
import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import java.time.LocalDateTime

class ProductOptionTest : FunSpec({

    test("PartnerShopCategory.from valid value returns correct enum") {
        val category = PartnerShopCategory.from("HAIR_MAKEUP")
        category shouldBe PartnerShopCategory.HAIR_MAKEUP
    }

    test("PartnerShopCategory.from invalid value throws CustomException") {
        val exception =
            shouldThrow<CustomException> {
                PartnerShopCategory.from("INVALID")
            }
        exception.errorCode shouldBe ErrorCode.INVALID_PARTNER_SHOP_CATEGORY
    }

    test("OptionType.from valid value returns correct enum") {
        val optionType = OptionType.from("SINGLE")
        optionType shouldBe OptionType.SINGLE
    }

    test("OptionType.from invalid value throws CustomException") {
        val exception =
            shouldThrow<CustomException> {
                OptionType.from("INVALID")
            }
        exception.errorCode shouldBe ErrorCode.INVALID_OPTION_TYPE
    }

    test("PartnerShop with blank name throws IllegalArgumentException") {
        val exception =
            shouldThrow<IllegalArgumentException> {
                PartnerShop(
                    category = PartnerShopCategory.HAIR_MAKEUP,
                    name = "",
                    link = "http://example.com",
                )
            }
        exception.message shouldContain "제휴 업체 이름은 비어 있을 수 없습니다."
    }

    test("Valid single product option does not throw exception") {
        val option =
            ProductOption(
                optionId = 1,
                productId = 1,
                name = "테스트 단품 옵션",
                optionType = OptionType.SINGLE,
                discountAvailable = false,
                originalPrice = 100,
                discountPrice = 80,
                description = "테스트 설명",
                costumeCount = 1,
                shootingLocationCount = 1,
                shootingHours = 1,
                shootingMinutes = 0,
                retouchedCount = 1,
                originalProvided = true,
                partnerShops = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        option.name shouldBe "테스트 단품 옵션"
    }

    test("Single product option with zero costumeCount throws exception") {
        val exception =
            shouldThrow<IllegalArgumentException> {
                ProductOption(
                    optionId = 1,
                    productId = 1,
                    name = "테스트 단품 옵션",
                    optionType = OptionType.SINGLE,
                    discountAvailable = false,
                    originalPrice = 100,
                    discountPrice = 80,
                    description = "테스트 설명",
                    costumeCount = 0,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    shootingMinutes = 0,
                    retouchedCount = 1,
                    originalProvided = true,
                    partnerShops = emptyList(),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )
            }
        exception.message shouldContain "단품 옵션은 의상 수가 1개 이상이어야 합니다."
    }

    test("Single product option with discount greater than original price throws exception") {
        val exception =
            shouldThrow<IllegalArgumentException> {
                ProductOption(
                    optionId = 1,
                    productId = 1,
                    name = "테스트 단품 옵션",
                    optionType = OptionType.SINGLE,
                    discountAvailable = true,
                    originalPrice = 100,
                    discountPrice = 120,
                    description = "테스트 설명",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    shootingMinutes = 0,
                    retouchedCount = 1,
                    originalProvided = true,
                    partnerShops = emptyList(),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )
            }
        exception.message shouldContain "할인가는 원 판매가보다 클 수 없습니다."
    }

    test("Package product option with empty partnerShops throws exception") {
        val exception =
            shouldThrow<IllegalArgumentException> {
                ProductOption(
                    optionId = 2,
                    productId = 1,
                    name = "테스트 패키지 옵션",
                    optionType = OptionType.PACKAGE,
                    discountAvailable = false,
                    originalPrice = 200,
                    discountPrice = 150,
                    description = "패키지 테스트",
                    costumeCount = 0,
                    shootingLocationCount = 0,
                    shootingHours = 0,
                    shootingMinutes = 0,
                    retouchedCount = 0,
                    originalProvided = true,
                    partnerShops = emptyList(),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )
            }
        exception.message shouldContain "패키지 옵션은 1개 이상의 파트너샵이 필요합니다."
    }

    test("Valid package product option does not throw exception") {
        val partnerShop =
            PartnerShop(
                category = PartnerShopCategory.DRESS,
                name = "테스트샵",
                link = "http://example.com",
            )
        val option =
            ProductOption(
                optionId = 2,
                productId = 1,
                name = "테스트 패키지 옵션",
                optionType = OptionType.PACKAGE,
                discountAvailable = false,
                originalPrice = 200,
                discountPrice = 150,
                description = "패키지 테스트",
                costumeCount = 0,
                shootingLocationCount = 0,
                shootingHours = 0,
                shootingMinutes = 0,
                retouchedCount = 0,
                originalProvided = true,
                partnerShops = listOf(partnerShop),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        option.partnerShops.size shouldBe 1
    }
})
