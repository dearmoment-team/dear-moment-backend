// ProductTest.kt
package kr.kro.dearmoment.product.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

/**
 * Product 클래스 테스트
 */
internal class ProductTest : StringSpec({

    // 공통 이미지 리스트
    val defaultImages = listOf("default_image.jpg")

    // 공통 PartnerShops 리스트
    val defaultPartnerShops =
        listOf(
            PartnerShop(name = "상점1", link = "http://shop1.com"),
            PartnerShop(name = "상점2", link = "http://shop2.com"),
        )

    // 테스트 데이터 팩토리 함수 (새로운 필드들도 기본값 설정)
    fun createProduct(
        productId: Long = 1L,
        userId: Long = 1L,
        title: String = "테스트 상품",
        description: String = "테스트 설명",
        price: Long = 100000,
        typeCode: Int = 1,
        options: List<ProductOption> = emptyList(),
        images: List<String> = defaultImages,
        partnerShops: List<PartnerShop> = if (typeCode == 1) defaultPartnerShops else emptyList(),
        concept: ConceptType = ConceptType.ELEGANT,
        // 기존의 provideOriginal 대신 originalProvideType와 partialOriginalCount 사용
        originalProvideType: OriginalProvideType = OriginalProvideType.FULL,
        partialOriginalCount: Int? = null,
        shootingTime: LocalDateTime? = null,
        shootingLocation: String = "",
        numberOfCostumes: Int = 0,
        seasonYear: Int? = null,
        seasonHalf: SeasonHalf? = null,
        detailedInfo: String = "",
        warrantyInfo: String = "",
        contactInfo: String = "",
        createdAt: LocalDateTime? = null,
        updatedAt: LocalDateTime? = null,
    ) = Product(
        productId = productId,
        userId = userId,
        title = title,
        description = description,
        price = price,
        typeCode = typeCode,
        concept = concept,
        originalProvideType = originalProvideType,
        partialOriginalCount = partialOriginalCount,
        shootingTime = shootingTime,
        shootingLocation = shootingLocation,
        numberOfCostumes = numberOfCostumes,
        seasonYear = seasonYear,
        seasonHalf = seasonHalf,
        partnerShops = partnerShops,
        detailedInfo = detailedInfo,
        warrantyInfo = warrantyInfo,
        contactInfo = contactInfo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        options = options,
        images = images,
    )

    "상품 생성 시 hasPackage가 true여야 한다" {
        // given
        val product = createProduct(typeCode = 1)
        // when
        val result = product.hasPackage
        // then
        result shouldBe true
    }

    "typeCode가 0인 상품은 hasPackage가 false여야 한다" {
        // given
        val product =
            createProduct(
                productId = 2L,
                userId = 2L,
                title = "일반 상품",
                description = "일반 설명",
                price = 50000,
                typeCode = 0,
                partnerShops = emptyList(),
            )
        // when
        val result = product.hasPackage
        // then
        result shouldBe false
    }

    "options가 제공되지 않은 경우 빈 리스트여야 한다" {
        // given
        val product =
            createProduct(
                productId = 3L,
                userId = 3L,
                title = "옵션 없는 상품",
                description = "옵션 없음 설명",
                price = 75000,
            )
        // when
        val result = product.options
        // then
        result shouldBe emptyList()
    }

    "options가 제공된 경우 해당 리스트로 초기화되어야 한다" {
        // given
        val options =
            listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 4L,
                    name = "옵션1",
                    additionalPrice = 10000,
                    description = "옵션 설명1",
                ),
                ProductOption(
                    optionId = 2L,
                    productId = 4L,
                    name = "옵션2",
                    additionalPrice = 20000,
                    description = "옵션 설명2",
                ),
            )

        val product =
            createProduct(
                productId = 4L,
                userId = 4L,
                title = "옵션 있는 상품",
                description = "옵션 있음 설명",
                price = 150000,
                options = options,
            )
        // when
        val result = product.options
        // then
        result shouldBe options
    }

    "updateOptions 함수가 새 옵션과 삭제할 옵션을 올바르게 반환해야 한다" {
        // given
        val existingOptions =
            listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 5L,
                    name = "기존 옵션1",
                    additionalPrice = 10000,
                    description = "기존 설명1",
                ),
                ProductOption(
                    optionId = 2L,
                    productId = 5L,
                    name = "기존 옵션2",
                    additionalPrice = 20000,
                    description = "기존 설명2",
                ),
            )

        val newOptions =
            listOf(
                // 업데이트: 기존 옵션1 유지, 내용 변경
                ProductOption(
                    optionId = 1L,
                    productId = 5L,
                    name = "기존 옵션1",
                    additionalPrice = 15000,
                    description = "업데이트 설명1",
                ),
                // 신규 옵션: 신규 옵션은 optionId를 0L로 지정
                ProductOption(
                    optionId = 0L,
                    productId = 5L,
                    name = "새 옵션",
                    additionalPrice = 30000,
                    description = "새 설명",
                ),
            )

        val product =
            createProduct(
                productId = 5L,
                userId = 5L,
                title = "옵션 업데이트 상품",
                description = "옵션 업데이트 설명",
                price = 200000,
                options = existingOptions,
            )

        // when
        val (updatedOptions, toDelete) = product.updateOptions(newOptions)
        // then
        updatedOptions shouldContainExactly listOf(
            ProductOption(
                optionId = 1L,
                productId = 5L,
                name = "기존 옵션1",
                additionalPrice = 15000,
                description = "업데이트 설명1",
            ),
            ProductOption(
                optionId = 0L,
                productId = 5L,
                name = "새 옵션",
                additionalPrice = 30000,
                description = "새 설명",
            ),
        )
        toDelete shouldContainExactly setOf(2L)
    }

    "이미지가 비어 있으면 예외가 발생해야 한다" {
        // given & when & then
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(
                    productId = 6L,
                    userId = 6L,
                    title = "이미지 없는 상품",
                    description = "이미지 없음 설명",
                    price = 60000,
                    typeCode = 1,
                    images = emptyList(),
                )
            }
        exception.message shouldBe "최소 1개 이상의 이미지가 필요합니다"
    }

    "typeCode가 1인 상품에서 partnerShops가 비어 있으면 예외가 발생해야 한다" {
        // given & when & then
        val exception =
            shouldThrow<IllegalArgumentException> {
                Product(
                    productId = 7L,
                    userId = 7L,
                    title = "파트너 없는 패키지 상품",
                    description = "파트너 없음 설명",
                    price = 120000,
                    typeCode = 1,
                    images = defaultImages,
                    partnerShops = emptyList(),
                    // 새로운 필드 추가
                    concept = ConceptType.ELEGANT,
                    originalProvideType = OriginalProvideType.FULL,
                    partialOriginalCount = null,
                    shootingTime = null,
                    shootingLocation = "",
                    numberOfCostumes = 0,
                    seasonYear = null,
                    seasonHalf = null,
                    detailedInfo = "",
                    warrantyInfo = "",
                    contactInfo = "",
                    options = emptyList(),
                    createdAt = null,
                    updatedAt = null,
                )
            }
        exception.message shouldBe "패키지 상품은 하나 이상의 협력업체 정보가 필요합니다."
    }

    "typeCode가 1인 상품에서 partnerShops에 빈 이름 또는 링크가 있으면 예외가 발생해야 한다" {
        // 빈 이름인 경우
        val exception1 =
            shouldThrow<IllegalArgumentException> {
                Product(
                    productId = 8L,
                    userId = 8L,
                    title = "빈 이름의 파트너",
                    description = "파트너 이름 빈 경우",
                    price = 130000,
                    typeCode = 1,
                    images = defaultImages,
                    partnerShops = listOf(
                        PartnerShop(
                            name = "",
                            link = "http://validlink.com",
                        )
                    ),
                    concept = ConceptType.ELEGANT,
                    originalProvideType = OriginalProvideType.FULL,
                    partialOriginalCount = null,
                    shootingTime = null,
                    shootingLocation = "",
                    numberOfCostumes = 0,
                    seasonYear = null,
                    seasonHalf = null,
                    detailedInfo = "",
                    warrantyInfo = "",
                    contactInfo = "",
                    options = emptyList(),
                    createdAt = null,
                    updatedAt = null,
                )
            }
        exception1.message shouldBe "파트너샵 이름은 비어 있을 수 없습니다."

        // 빈 링크인 경우
        val exception2 =
            shouldThrow<IllegalArgumentException> {
                Product(
                    productId = 9L,
                    userId = 9L,
                    title = "빈 링크의 파트너",
                    description = "파트너 링크 빈 경우",
                    price = 140000,
                    typeCode = 1,
                    images = defaultImages,
                    partnerShops = listOf(
                        PartnerShop(
                            name = "Valid Name",
                            link = "",
                        )
                    ),
                    concept = ConceptType.ELEGANT,
                    originalProvideType = OriginalProvideType.FULL,
                    partialOriginalCount = null,
                    shootingTime = null,
                    shootingLocation = "",
                    numberOfCostumes = 0,
                    seasonYear = null,
                    seasonHalf = null,
                    detailedInfo = "",
                    warrantyInfo = "",
                    contactInfo = "",
                    options = emptyList(),
                    createdAt = null,
                    updatedAt = null,
                )
            }
        exception2.message shouldBe "파트너샵 링크는 비어 있을 수 없습니다."
    }

    "새로운 필드들이 올바르게 설정되어야 한다" {
        // given
        val fixedTime = LocalDateTime.of(2023, 1, 1, 10, 0)
        val product =
            createProduct(
                productId = 10L,
                userId = 10L,
                title = "새로운 필드 테스트 상품",
                description = "새로운 필드 테스트 설명",
                price = 200000,
                typeCode = 0,
                images = defaultImages,
                partnerShops = emptyList(),
                concept = ConceptType.MODERN,
                // 여기서는 원본 제공을 PARTIAL로 하고, 제공 장수를 5로 지정
                originalProvideType = OriginalProvideType.PARTIAL,
                partialOriginalCount = 5,
                shootingTime = fixedTime,
                shootingLocation = "테스트 스튜디오",
                numberOfCostumes = 3,
                seasonYear = 2023,
                seasonHalf = SeasonHalf.FIRST_HALF,
                detailedInfo = "자세한 정보",
                warrantyInfo = "2년 보증",
                contactInfo = "contact@test.com",
                createdAt = fixedTime,
                updatedAt = fixedTime,
            )
        // when & then
        product.concept shouldBe ConceptType.MODERN
        product.originalProvideType shouldBe OriginalProvideType.PARTIAL
        product.partialOriginalCount shouldBe 5
        product.shootingTime shouldBe fixedTime
        product.shootingLocation shouldBe "테스트 스튜디오"
        product.numberOfCostumes shouldBe 3
        product.seasonYear shouldBe 2023
        product.seasonHalf shouldBe SeasonHalf.FIRST_HALF
        product.detailedInfo shouldBe "자세한 정보"
        product.warrantyInfo shouldBe "2년 보증"
        product.contactInfo shouldBe "contact@test.com"
        product.createdAt shouldBe fixedTime
        product.updatedAt shouldBe fixedTime
    }
})
