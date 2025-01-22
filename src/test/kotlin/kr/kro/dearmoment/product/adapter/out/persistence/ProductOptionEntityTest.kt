package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionEntityTest : DescribeSpec({
    describe("ProductOptionEntity") {
        // 샘플 ProductEntity 생성
        val sampleProductEntity = ProductEntity(
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
            options = mutableListOf()
        )

        // 수정된 ProductOption 샘플 데이터
        val sampleOptions = listOf(
            ProductOption(
                optionId = 1L,
                name = "옵션 A",
                additionalPrice = 500L,
                description = "옵션 A 설명",
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 11, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0)
            ),
            ProductOption(
                optionId = 2L,
                name = "옵션 B",
                additionalPrice = 1000L,
                description = "옵션 B 설명",
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 12, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 12, 0)
            )
        )

        // 샘플 ProductOptionEntity 생성
        val sampleOptionEntities = sampleOptions.map { option ->
            ProductOptionEntity.fromDomain(option, sampleProductEntity)
        }.toMutableList()

        // ProductEntity에 옵션 추가
        sampleProductEntity.options.addAll(sampleOptionEntities)

        // 샘플 도메인 모델 생성
        val sampleDomainOption = ProductOption(
            optionId = 3L,
            name = "옵션 C",
            additionalPrice = 1500L,
            description = "옵션 C 설명",
            productId = 1L,
            createdAt = LocalDateTime.of(2023, 1, 1, 13, 0),
            updatedAt = LocalDateTime.of(2023, 1, 2, 13, 0)
        )

        it("fromDomain 메서드는 도메인 모델을 올바르게 ProductOptionEntity로 매핑해야 합니다") {
            val optionEntity = ProductOptionEntity.fromDomain(sampleDomainOption, sampleProductEntity)

            optionEntity.optionId shouldBe sampleDomainOption.optionId
            optionEntity.name shouldBe sampleDomainOption.name
            optionEntity.additionalPrice shouldBe sampleDomainOption.additionalPrice
            optionEntity.description shouldBe sampleDomainOption.description
            optionEntity.product shouldBe sampleProductEntity
            optionEntity.createdAt shouldBe sampleDomainOption.createdAt
            optionEntity.updatedAt shouldBe sampleDomainOption.updatedAt
        }

        it("toDomain 메서드는 ProductOptionEntity를 올바르게 도메인 모델로 매핑해야 합니다") {
            val optionEntity = ProductOptionEntity(
                optionId = 4L,
                name = "옵션 D",
                additionalPrice = 2000L,
                description = "옵션 D 설명",
                product = sampleProductEntity,
                createdAt = LocalDateTime.of(2023, 1, 1, 14, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 14, 0)
            )

            val domainOption = optionEntity.toDomain()

            domainOption.optionId shouldBe optionEntity.optionId
            domainOption.name shouldBe optionEntity.name
            domainOption.additionalPrice shouldBe optionEntity.additionalPrice
            domainOption.description shouldBe optionEntity.description
            domainOption.productId shouldBe sampleProductEntity.productId
            domainOption.createdAt shouldBe optionEntity.createdAt
            domainOption.updatedAt shouldBe optionEntity.updatedAt
        }

        it("fromDomain 메서드는 optionId가 0일 때 null로 설정해야 합니다") {
            val optionWithZeroId = ProductOption(
                optionId = 0L,
                name = "옵션 E",
                additionalPrice = 2500L,
                description = "옵션 E 설명",
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 15, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 15, 0)
            )
            val optionEntity = ProductOptionEntity.fromDomain(optionWithZeroId, sampleProductEntity)

            optionEntity.optionId shouldBe null
        }

        it("fromDomain 메서드는 description이 null일 때도 올바르게 매핑해야 합니다") {
            val optionWithoutDescription = ProductOption(
                optionId = 6L,
                name = "옵션 F",
                additionalPrice = 3000L,
                description = null,
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 16, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 16, 0)
            )
            val optionEntity = ProductOptionEntity.fromDomain(optionWithoutDescription, sampleProductEntity)

            optionEntity.optionId shouldBe optionWithoutDescription.optionId
            optionEntity.name shouldBe optionWithoutDescription.name
            optionEntity.additionalPrice shouldBe optionWithoutDescription.additionalPrice
            optionEntity.description shouldBe null
            optionEntity.product shouldBe sampleProductEntity
            optionEntity.createdAt shouldBe optionWithoutDescription.createdAt
            optionEntity.updatedAt shouldBe optionWithoutDescription.updatedAt
        }

        it("ProductOptionEntity의 toDomain 메서드는 모든 필드를 올바르게 매핑해야 합니다") {
            val optionEntity = ProductOptionEntity(
                optionId = 7L,
                name = "옵션 G",
                additionalPrice = 3500L,
                description = "옵션 G 설명",
                product = sampleProductEntity,
                createdAt = LocalDateTime.of(2023, 1, 1, 17, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 17, 0)
            )

            val domainOption = optionEntity.toDomain()

            domainOption.optionId shouldBe optionEntity.optionId
            domainOption.name shouldBe optionEntity.name
            domainOption.additionalPrice shouldBe optionEntity.additionalPrice
            domainOption.description shouldBe optionEntity.description
            domainOption.productId shouldBe sampleProductEntity.productId
            domainOption.createdAt shouldBe optionEntity.createdAt
            domainOption.updatedAt shouldBe optionEntity.updatedAt
        }
    }
})