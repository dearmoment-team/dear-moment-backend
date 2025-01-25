package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

/**
 * ProductOptionEntityTest는 ProductOptionEntity 클래스의 동작을 검증하는 테스트 클래스입니다.
 * 다양한 시나리오를 통해 엔티티와 도메인 객체 간의 매핑을 테스트합니다.
 */
class ProductOptionEntityTest : DescribeSpec({

    describe("ProductOptionEntity") {
        /**
         * 샘플 ProductEntity 데이터를 생성합니다.
         * 이 데이터는 테스트에서 ProductOptionEntity와 연관 관계를 검증하는 데 사용됩니다.
         */
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

        /**
         * optionId가 0일 때 null로 변환되는지 검증합니다.
         */
        it("optionId가 0일 때 null로 변환되어야 합니다") {
            val optionWithZeroId = ProductOption(
                optionId = 0L,
                name = "옵션 G",
                additionalPrice = 2000L,
                description = "옵션 G 설명",
                productId = 1L,
                createdAt = null,
                updatedAt = null
            )
            val optionEntity = ProductOptionEntity.fromDomain(optionWithZeroId, sampleProductEntity)

            optionEntity.optionId shouldBe null
        }

        /**
         * description이 값이 있을 때 해당 값이 그대로 매핑되는지 검증합니다.
         */
        it("description이 값이 있을 때 그대로 매핑되어야 합니다") {
            val optionWithDescription = ProductOption(
                optionId = 7L,
                name = "옵션 H",
                additionalPrice = 1500L,
                description = "옵션 H 설명",
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 15, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 15, 0)
            )
            val optionEntity = ProductOptionEntity.fromDomain(optionWithDescription, sampleProductEntity)

            optionEntity.description shouldBe "옵션 H 설명"
        }

        /**
         * createdAt과 updatedAt이 null일 때 현재 시각으로 기본값이 설정되는지 검증합니다.
         */
        it("createdAt과 updatedAt이 null일 때 현재 시각으로 기본값이 설정되어야 합니다") {
            val optionWithoutTimestamps = ProductOption(
                optionId = 8L,
                name = "옵션 I",
                additionalPrice = 2500L,
                description = "옵션 I 설명",
                productId = 1L,
                createdAt = null,
                updatedAt = null
            )
            val optionEntity = ProductOptionEntity.fromDomain(optionWithoutTimestamps, sampleProductEntity)

            optionEntity.createdAt shouldNotBe null
            optionEntity.updatedAt shouldNotBe null
        }

        /**
         * toDomain 메서드가 모든 필드를 정확히 변환하는지 검증합니다.
         */
        it("toDomain 메서드는 모든 필드를 정확히 변환해야 합니다") {
            val optionEntity = ProductOptionEntity(
                optionId = 9L,
                name = "옵션 J",
                additionalPrice = 3000L,
                description = "옵션 J 설명",
                product = sampleProductEntity,
                createdAt = LocalDateTime.of(2023, 1, 1, 18, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 18, 0)
            )
            val domainOption = optionEntity.toDomain()

            domainOption.optionId shouldBe 9L
            domainOption.name shouldBe "옵션 J"
            domainOption.additionalPrice shouldBe 3000L
            domainOption.description shouldBe "옵션 J 설명"
            domainOption.productId shouldBe 1L
            domainOption.createdAt shouldBe LocalDateTime.of(2023, 1, 1, 18, 0)
            domainOption.updatedAt shouldBe LocalDateTime.of(2023, 1, 2, 18, 0)
        }
    }
})