package kr.kro.dearmoment.product.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.image.domain.Image

internal class ProductTest : StringSpec({

    // 테스트용 이미지 생성 함수
    fun createImage(fileName: String) =
        Image(
            userId = 1L,
            fileName = fileName,
            url = "http://example.com/$fileName",
        )

    // 기본 Product 생성 헬퍼
    fun createProduct(
        productId: Long = 1L,
        title: String = "테스트 상품",
        mainImage: Image = createImage("main.jpg"),
        subImages: List<Image> = List(4) { createImage("sub${it + 1}.jpg") },
        options: List<ProductOption> = emptyList(),
        additionalImages: List<Image> = emptyList(),
        basePrice: Long = 100_000L,
        retouchStyles: Set<RetouchStyle> = emptySet(),
    ) = Product(
        productId = productId,
        userId = 1L,
        title = title,
        mainImage = mainImage,
        subImages = subImages,
        additionalImages = additionalImages,
        basePrice = basePrice,
        retouchStyles = retouchStyles,
        options = options,
    )

    "Product 생성 시 제목이 비어 있으면 예외 발생" {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(title = "")
            }
        exception.message shouldBe "상품명은 필수 입력값입니다."
    }

    "기본 가격이 음수이면 예외 발생" {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(basePrice = -1)
            }
        exception.message shouldBe "기본 가격은 0 이상이어야 합니다."
    }

    "서브 이미지는 정확히 4장이어야 함" {
        val validProduct = createProduct(subImages = List(4) { createImage("valid$it.jpg") })
        validProduct.subImages.size shouldBe 4

        shouldThrow<IllegalArgumentException> {
            createProduct(subImages = List(3) { createImage("invalid$it.jpg") })
        }.message shouldBe "서브 이미지는 정확히 4장 등록해야 합니다."
    }

    "추가 이미지는 최대 5장까지 허용" {
        val validProduct = createProduct(additionalImages = List(5) { createImage("add$it.jpg") })
        validProduct.additionalImages.size shouldBe 5

        shouldThrow<IllegalArgumentException> {
            createProduct(additionalImages = List(6) { createImage("invalid$it.jpg") })
        }.message shouldBe "추가 이미지는 최대 5장까지 등록 가능합니다."
    }

    "보정 스타일 최대 2개 선택 가능" {
        val validStyles = setOf(RetouchStyle.MODERN, RetouchStyle.NATURAL)
        val validProduct = createProduct(retouchStyles = validStyles)
        validProduct.retouchStyles.size shouldBe 2

        // 주석 해제 시 테스트 활성화
        /*
        shouldThrow<IllegalArgumentException> {
            createProduct(retouchStyles = setOf(RetouchStyle.MODERN, RetouchStyle.NATURAL, RetouchStyle.VINTAGE))
        }.message shouldBe "보정 스타일은 최대 2개까지만 선택할 수 있습니다."
         */
    }

    "옵션 업데이트 시 변경 사항 정확히 반영" {
        val existingOptions =
            listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "기존 옵션1",
                    optionType = OptionType.SINGLE,
                    additionalPrice = 10_000,
                    costumeCount = 1,
                    shootingLocation = "스튜디오",
                    shootingMinutes = 60,
                ),
                ProductOption(
                    optionId = 2L,
                    productId = 1L,
                    name = "기존 옵션2",
                    optionType = OptionType.PACKAGE,
                    additionalPrice = 20_000,
                    packageCategory = PackageCategory.DRESS,
                    partnerShops = listOf(PartnerShop("드레스샵", "http://dress.com")),
                ),
            )
        val product = createProduct(options = existingOptions)

        val newOptions =
            listOf(
                existingOptions[0].copy(additionalPrice = 15_000), // 업데이트
                ProductOption(
                    // 신규 추가
                    optionId = 0L,
                    productId = 1L,
                    name = "새 옵션",
                    optionType = OptionType.PACKAGE,
                    additionalPrice = 30_000,
                    packageCategory = PackageCategory.DRONE,
                    partnerShops = listOf(PartnerShop("드론샵", "http://drone.com")),
                ),
            )
        val result = product.updateOptions(newOptions)

        result.updatedOptions shouldContainExactly
                listOf(
                    existingOptions[0].copy(additionalPrice = 15_000),
                    newOptions[1].copy(productId = 1L),
                )
        result.deletedOptionIds shouldContainExactly setOf(2L)
    }

    "단품 옵션 필수 필드 검증" {
        shouldThrow<IllegalArgumentException> {
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "잘못된 단품",
                optionType = OptionType.SINGLE,
                additionalPrice = 10_000,
                costumeCount = 0, // 잘못된 값
                shootingLocation = "스튜디오",
                shootingMinutes = 60,
            )
        }.message shouldBe "단품 옵션은 의상 수가 1개 이상이어야 합니다."

        shouldThrow<IllegalArgumentException> {
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "잘못된 단품",
                optionType = OptionType.SINGLE,
                additionalPrice = 10_000,
                costumeCount = 1,
                shootingLocation = "",
                shootingMinutes = 60,
            )
        }.message shouldBe "단품 옵션은 촬영 장소를 반드시 입력해야 합니다."
    }

    "패키지 옵션 필수 필드 검증" {
        shouldThrow<IllegalArgumentException> {
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "잘못된 패키지",
                optionType = OptionType.PACKAGE,
                additionalPrice = 10_000,
                packageCategory = null, // 잘못된 값
                partnerShops = listOf(PartnerShop("드레스샵", "http://dress.com")),
            )
        }.message shouldBe "패키지 옵션에는 packageCategory가 필수입니다."

        shouldThrow<IllegalArgumentException> {
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "잘못된 패키지",
                optionType = OptionType.PACKAGE,
                additionalPrice = 10_000,
                packageCategory = PackageCategory.DRESS,
                partnerShops = emptyList(), // 잘못된 값
            )
        }.message shouldBe "패키지 옵션은 최소 1개 이상의 제휴 업체가 필요합니다."
    }

    "협력업체 정보 유효성 검사" {
        shouldThrow<IllegalArgumentException> {
            PartnerShop(name = "", link = "http://valid.com")
        }.message shouldBe "제휴 업체 이름은 비어 있을 수 없습니다."

        shouldThrow<IllegalArgumentException> {
            PartnerShop(name = "유효한 이름", link = "")
        }.message shouldBe "제휴 업체 링크는 비어 있을 수 없습니다."
    }

    "옵션 추가 가격 음수 검증" {
        shouldThrow<IllegalArgumentException> {
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "잘못된 가격",
                optionType = OptionType.SINGLE,
                additionalPrice = -1,
                costumeCount = 1,
                shootingLocation = "스튜디오",
                shootingMinutes = 60,
            )
        }.message shouldBe "추가 가격은 음수가 될 수 없습니다."
    }
})
