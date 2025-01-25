package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionEntityTest : DescribeSpec({
    describe("ProductOptionEntity") {
        // 샘플 ProductEntity 생성
        val sampleProductEntity =
            ProductEntity(
                productId = 1L,
                userId = 100L,
                title = "샘플 상품",
                description = "이것은 샘플 상품입니다.",
                price = 9999L,
                typeCode = 1,
                shootingTime = LocalDateTime.of(2023, 1, 1, 12, 0),
                shootingLocation = "서울",
                numberOfCostumes = 5,
                packagePartnerShops = "파트너 샵 A, 파트너 샵 B",
                detailedInfo = "상품에 대한 상세 정보.",
                warrantyInfo = "1년 보증.",
                contactInfo = "contact@example.com",
                createdAt = LocalDateTime.of(2023, 1, 1, 10, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 10, 0),
                options = mutableListOf(),
            )

        it("fromDomain 메서드는 description이 null일 때 null로 유지해야 합니다") {
            val optionWithoutDescription =
                ProductOption(
                    optionId = 6L,
                    name = "옵션 F",
                    additionalPrice = 3000L,
                    description = null,
                    productId = 1L,
                    createdAt = LocalDateTime.of(2023, 1, 1, 16, 0),
                    updatedAt = LocalDateTime.of(2023, 1, 2, 16, 0),
                )
            val optionEntity = ProductOptionEntity.fromDomain(optionWithoutDescription, sampleProductEntity)

            optionEntity.optionId shouldBe optionWithoutDescription.optionId
            optionEntity.name shouldBe optionWithoutDescription.name
            optionEntity.additionalPrice shouldBe optionWithoutDescription.additionalPrice
            optionEntity.description shouldBe null // 기본값이 아닌 null로 유지
            optionEntity.product shouldBe sampleProductEntity
            optionEntity.createdAt shouldBe optionWithoutDescription.createdAt
            optionEntity.updatedAt shouldBe optionWithoutDescription.updatedAt
        }
    }
})
