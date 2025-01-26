package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

/**
 * ProductEntity의 동작을 검증하기 위한 테스트 클래스입니다.
 */
class ProductEntityTest : DescribeSpec({

    describe("ProductEntity") {

        // 샘플 옵션 데이터
        val sampleOptions = listOf(
            ProductOption(
                optionId = 1L,
                name = "옵션 A",
                additionalPrice = 500L,
                description = "옵션 A 설명",
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 11, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0),
            ),
            ProductOption(
                optionId = 2L,
                name = "옵션 B",
                additionalPrice = 1000L,
                description = "옵션 B 설명",
                productId = 1L,
                createdAt = LocalDateTime.of(2023, 1, 1, 12, 0),
                updatedAt = LocalDateTime.of(2023, 1, 2, 12, 0),
            )
        )

        // 샘플 제품 데이터
        val sampleProduct = Product(
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
            options = sampleOptions,
        )

        it("fromDomain은 도메인 모델을 올바르게 ProductEntity로 매핑합니다") {
            val productEntity = ProductEntity.fromDomain(sampleProduct)

            productEntity.productId shouldBe sampleProduct.productId
            productEntity.userId shouldBe sampleProduct.userId
            productEntity.title shouldBe sampleProduct.title
            productEntity.description shouldBe sampleProduct.description
            productEntity.price shouldBe sampleProduct.price
            productEntity.typeCode shouldBe sampleProduct.typeCode
            productEntity.shootingTime shouldBe sampleProduct.shootingTime
            productEntity.shootingLocation shouldBe sampleProduct.shootingLocation
            productEntity.numberOfCostumes shouldBe sampleProduct.numberOfCostumes
            productEntity.packagePartnerShops shouldBe sampleProduct.packagePartnerShops
            productEntity.detailedInfo shouldBe sampleProduct.detailedInfo
            productEntity.warrantyInfo shouldBe sampleProduct.warrantyInfo
            productEntity.contactInfo shouldBe sampleProduct.contactInfo
            productEntity.createdAt shouldBe sampleProduct.createdAt
            productEntity.updatedAt shouldBe sampleProduct.updatedAt

            productEntity.options.size shouldBe sampleProduct.options.size
            productEntity.options.forEachIndexed { index, optionEntity ->
                val optionDomain = sampleProduct.options[index]
                optionEntity.optionId shouldBe optionDomain.optionId
                optionEntity.name shouldBe optionDomain.name
                optionEntity.additionalPrice shouldBe optionDomain.additionalPrice
                optionEntity.description shouldBe optionDomain.description
                optionEntity.product?.productId shouldBe productEntity.productId
                optionEntity.createdAt shouldBe optionDomain.createdAt
                optionEntity.updatedAt shouldBe optionDomain.updatedAt
            }
        }

        it("toDomain은 ProductEntity를 올바르게 도메인 모델로 매핑합니다") {
            val productEntity = ProductEntity.fromDomain(sampleProduct)
            val domainProduct = productEntity.toDomain()

            domainProduct.productId shouldBe sampleProduct.productId
            domainProduct.userId shouldBe sampleProduct.userId
            domainProduct.title shouldBe sampleProduct.title
            domainProduct.description shouldBe sampleProduct.description
            domainProduct.price shouldBe sampleProduct.price
            domainProduct.typeCode shouldBe sampleProduct.typeCode
            domainProduct.shootingTime shouldBe sampleProduct.shootingTime
            domainProduct.shootingLocation shouldBe sampleProduct.shootingLocation
            domainProduct.numberOfCostumes shouldBe sampleProduct.numberOfCostumes
            domainProduct.packagePartnerShops shouldBe sampleProduct.packagePartnerShops
            domainProduct.detailedInfo shouldBe sampleProduct.detailedInfo
            domainProduct.warrantyInfo shouldBe sampleProduct.warrantyInfo
            domainProduct.contactInfo shouldBe sampleProduct.contactInfo
            domainProduct.createdAt shouldBe sampleProduct.createdAt
            domainProduct.updatedAt shouldBe sampleProduct.updatedAt

            domainProduct.options.size shouldBe sampleProduct.options.size
            domainProduct.options.forEachIndexed { index, optionDomain ->
                val optionEntity = productEntity.options[index]
                optionDomain.optionId shouldBe optionEntity.optionId
                optionDomain.name shouldBe optionEntity.name
                optionDomain.additionalPrice shouldBe optionEntity.additionalPrice
                optionDomain.description shouldBe optionEntity.description
                optionDomain.productId shouldBe optionEntity.product?.productId
                optionDomain.createdAt shouldBe optionEntity.createdAt
                optionDomain.updatedAt shouldBe optionEntity.updatedAt
            }
        }

        it("옵션이 없는 Product도 올바르게 매핑합니다") {
            val productWithoutOptions = sampleProduct.copy(options = emptyList())
            val productEntity = ProductEntity.fromDomain(productWithoutOptions)
            val domainProduct = productEntity.toDomain()

            productEntity.options.size shouldBe 0
            domainProduct.options.size shouldBe 0
        }

        it("옵션 필드가 null인 Product도 올바르게 처리합니다") {
            val productWithNullFields = sampleProduct.copy(options = listOf(
                ProductOption(
                    optionId = 1L,
                    name = "옵션 A",
                    additionalPrice = 500L,
                    description = null, // null 필드 테스트
                    productId = 1L,
                    createdAt = LocalDateTime.of(2023, 1, 1, 11, 0),
                    updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0),
                )
            ))
            val productEntity = ProductEntity.fromDomain(productWithNullFields)
            val domainProduct = productEntity.toDomain()

            domainProduct.options.first().description shouldBe null
            productEntity.options.first().description shouldBe null
        }
    }
})
