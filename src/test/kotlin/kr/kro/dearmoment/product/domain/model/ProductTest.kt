package kr.kro.dearmoment.product.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.image.domain.Image
import java.time.LocalDateTime

/**
 * 바뀐 도메인(Product, ProductOption, PartnerShop 등)에 대한 테스트
 */
internal class ProductTest : StringSpec({

    // 테스트용 이미지 생성 함수
    fun createImage(fileName: String) =
        Image(
            userId = 1L,
            fileName = fileName,
            url = "http://example.com/$fileName",
        )

    /**
     * Product 생성 헬퍼
     * - 새 도메인 구조에 맞춰 기본값들을 지정
     */
    fun createProduct(
        productId: Long = 1L,
        userId: Long = 1L,
        productType: ProductType = ProductType.WEDDING_SNAP,
        shootingPlace: ShootingPlace = ShootingPlace.JEJU,
        title: String = "테스트 상품",
        mainImage: Image = createImage("main.jpg"),
        subImages: List<Image> = List(4) { createImage("sub${it + 1}.jpg") },
        additionalImages: List<Image> = emptyList(),
        availableSeasons: Set<ShootingSeason> = emptySet(),
        cameraTypes: Set<CameraType> = emptySet(),
        retouchStyles: Set<RetouchStyle> = emptySet(),
        description: String = "",
        detailedInfo: String = "",
        contactInfo: String = "",
        options: List<ProductOption> = emptyList(),
    ): Product {
        return Product(
            productId = productId,
            userId = userId,
            productType = productType,
            shootingPlace = shootingPlace,
            title = title,
            description = description,
            availableSeasons = availableSeasons,
            cameraTypes = cameraTypes,
            retouchStyles = retouchStyles,
            mainImage = mainImage,
            subImages = subImages,
            additionalImages = additionalImages,
            detailedInfo = detailedInfo,
            contactInfo = contactInfo,
            options = options,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * ProductOption 생성 헬퍼
     * - 단품(SINGLE) / 패키지(PACKAGE) 구분, 가격/할인, 파트너샵 목록 등
     */
    fun createOption(
        optionId: Long = 0L,
        productId: Long = 1L,
        name: String = "테스트 옵션",
        optionType: OptionType = OptionType.SINGLE,
        discountAvailable: Boolean = false,
        originalPrice: Long = 100_000L,
        discountPrice: Long = 0L,
        costumeCount: Int = 1,
        shootingLocationCount: Int = 1,
        shootingHours: Int = 1,
        shootingMinutes: Int = 0,
        retouchedCount: Int = 1,
        partnerShops: List<PartnerShop> = emptyList(),
    ): ProductOption {
        return ProductOption(
            optionId = optionId,
            productId = productId,
            name = name,
            optionType = optionType,
            discountAvailable = discountAvailable,
            originalPrice = originalPrice,
            discountPrice = discountPrice,
            costumeCount = costumeCount,
            shootingLocationCount = shootingLocationCount,
            shootingHours = shootingHours,
            shootingMinutes = shootingMinutes,
            retouchedCount = retouchedCount,
            originalProvided = true,
            partnerShops = partnerShops,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }

    "Product 생성 시 제목이 비어 있으면 예외 발생" {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(title = "")
            }
        exception.message shouldBe "상품명은 필수 입력값입니다."
    }

    "서브 이미지는 정확히 4장이어야 함" {
        val validProduct = createProduct(subImages = List(4) { createImage("sub$it.jpg") })
        validProduct.subImages.size shouldBe 4

        shouldThrow<IllegalArgumentException> {
            createProduct(subImages = List(3) { createImage("sub$it.jpg") })
        }.message shouldBe "서브 이미지는 정확히 4장 등록해야 합니다."
    }

    "추가 이미지는 최대 5장까지 허용" {
        val validProduct = createProduct(additionalImages = List(5) { createImage("add$it.jpg") })
        validProduct.additionalImages.size shouldBe 5

        shouldThrow<IllegalArgumentException> {
            createProduct(additionalImages = List(6) { createImage("add$it.jpg") })
        }.message shouldBe "추가 이미지는 최대 5장까지 등록 가능합니다."
    }

    "보정 스타일 최대 2개만 허용(필요 시 주석 해제)" {
        val validStyles = setOf(RetouchStyle.MODERN, RetouchStyle.NATURAL)
        val validProduct = createProduct(retouchStyles = validStyles)
        validProduct.retouchStyles.size shouldBe 2

        // 만약 실제 코드에서 2개 제한 로직을 활성화했다면:
        shouldThrow<IllegalArgumentException> {
            createProduct(
                retouchStyles =
                setOf(
                    RetouchStyle.MODERN,
                    RetouchStyle.NATURAL,
                    RetouchStyle.VINTAGE,
                ),
            )
        }.message shouldBe "보정 스타일은 최대 2개까지만 선택할 수 있습니다."
    }

    "옵션 업데이트 시 삭제/갱신/삽입 로직 검증" {
        val existingOptions =
            listOf(
                createOption(
                    optionId = 1L,
                    name = "기존 단품 옵션",
                    originalPrice = 100_000L,
                    costumeCount = 1,
                    shootingHours = 1,
                    shootingMinutes = 30,
                    shootingLocationCount = 1,
                    retouchedCount = 2,
                ),
                createOption(
                    optionId = 2L,
                    name = "기존 패키지 옵션",
                    optionType = OptionType.PACKAGE,
                    partnerShops =
                    listOf(
                        PartnerShop(
                            category = PartnerShopCategory.DRESS,
                            name = "드레스샵",
                            link = "http://dress.com",
                        ),
                    ),
                ),
            )
        val product = createProduct(options = existingOptions)

        // 새 옵션 목록
        val newOptions =
            listOf(
                // 기존 첫 번째 옵션을 갱신 (optionId=1)
                existingOptions[0].copy(originalPrice = 120_000L),
                // 새로 추가 (optionId=0)
                createOption(
                    optionId = 0L,
                    name = "새로운 패키지 옵션",
                    optionType = OptionType.PACKAGE,
                    partnerShops =
                    listOf(
                        PartnerShop(
                            category = PartnerShopCategory.VIDEO,
                            name = "영상업체",
                            link = "http://video.com",
                        ),
                    ),
                ),
            )

        val result = product.updateOptions(newOptions)

        // updatedOptions => 기존 1번 옵션 갱신 + 신규 삽입
        result.updatedOptions shouldContainExactly
                listOf(
                    existingOptions[0].copy(originalPrice = 120_000L, productId = 1L),
                    newOptions[1].copy(productId = 1L),
                )

        // deletedOptionIds => 2번 옵션은 새 목록에 없으므로 삭제 대상
        result.deletedOptionIds shouldContainExactly setOf(2L)
    }

    "단품 옵션 - 필수 필드 검증" {
        // 의상 수가 1 미만이면 예외
        shouldThrow<IllegalArgumentException> {
            createOption(
                optionType = OptionType.SINGLE,
                costumeCount = 0,
            )
        }.message shouldBe "단품 옵션은 의상 수가 1개 이상이어야 합니다."

        // 장소 수가 1 미만이면 예외
        shouldThrow<IllegalArgumentException> {
            createOption(
                optionType = OptionType.SINGLE,
                costumeCount = 1,
                shootingLocationCount = 0,
            )
        }.message shouldBe "단품 옵션은 촬영 장소 수가 1개 이상이어야 합니다."

        // 촬영시간(시+분)이 모두 0이면 예외
        shouldThrow<IllegalArgumentException> {
            createOption(
                optionType = OptionType.SINGLE,
                costumeCount = 1,
                shootingLocationCount = 1,
                shootingHours = 0,
                shootingMinutes = 0,
            )
        }.message shouldBe "단품 옵션의 촬영 시간은 최소 1분 이상이어야 합니다."

        // 보정본 수가 1 미만이면 예외
        shouldThrow<IllegalArgumentException> {
            createOption(
                optionType = OptionType.SINGLE,
                costumeCount = 1,
                shootingLocationCount = 1,
                shootingHours = 0,
                shootingMinutes = 30,
                retouchedCount = 0,
            )
        }.message shouldBe "단품 옵션은 보정본이 1장 이상이어야 합니다."
    }

    "패키지 옵션 - 파트너샵 목록이 비어 있으면 예외" {
        shouldThrow<IllegalArgumentException> {
            createOption(
                optionType = OptionType.PACKAGE,
                partnerShops = emptyList(),
            )
        }.message shouldBe "패키지 옵션은 1개 이상의 파트너샵이 필요합니다."
    }

    "파트너샵 - 이름이 비어 있으면 예외" {
        shouldThrow<IllegalArgumentException> {
            PartnerShop(
                category = PartnerShopCategory.DRESS,
                name = "",
                link = "http://valid.com",
            )
        }.message shouldBe "제휴 업체 이름은 비어 있을 수 없습니다."
    }

    "옵션 가격 검증 - 원가/할인가" {
        // 원 판매가가 음수
        shouldThrow<IllegalArgumentException> {
            createOption(originalPrice = -1)
        }.message shouldBe "원 판매가는 0 이상이어야 합니다."

        // 할인가가 음수
        shouldThrow<IllegalArgumentException> {
            createOption(discountPrice = -10)
        }.message shouldBe "할인가는 0 이상이어야 합니다."

        // discountAvailable = true 인데, 할인가가 원가보다 큼
        shouldThrow<IllegalArgumentException> {
            createOption(
                discountAvailable = true,
                originalPrice = 100_000L,
                discountPrice = 120_000L,
            )
        }.message shouldBe "할인가는 원 판매가보다 클 수 없습니다."
    }

    "옵션명 필수 검증 - 빈 문자열이면 예외" {
        shouldThrow<IllegalArgumentException> {
            createOption(name = "")
        }.message shouldBe "옵션명은 비어 있을 수 없습니다."
    }
})
