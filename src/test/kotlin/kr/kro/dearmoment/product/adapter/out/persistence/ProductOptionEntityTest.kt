package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionEntityTest : StringSpec({

    "ProductOptionEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        val productEntity = ProductEntity(
            productId = 1L,
            title = "테스트 제품",
            price = 1000L,
            typeCode = 1
        )

        /* ProductOption 도메인 모델 생성
           - optionId: 옵션 ID
           - productId: 연관된 제품 ID
           - name: 옵션 이름
           - additionalPrice: 추가 가격
           - description: 옵션 설명
           - createdAt: 생성 시간
           - updatedAt: 수정 시간
        */
        val productOption = ProductOption(
            optionId = 1L,
            productId = 1L,
            name = "테스트 옵션",
            additionalPrice = 500L,
            description = "옵션 설명",
            createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            updatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        /* ProductOptionEntity로 변환 */
        val productOptionEntity = ProductOptionEntity.fromDomain(productOption, productEntity)

        /* 변환된 필드 값 확인 */
        productOptionEntity.optionId shouldBe productOption.optionId
        productOptionEntity.name shouldBe productOption.name
        productOptionEntity.additionalPrice shouldBe productOption.additionalPrice
        productOptionEntity.description shouldBe productOption.description
        productOptionEntity.product?.productId shouldBe productOption.productId
        productOptionEntity.createdAt shouldBe productOption.createdAt
        productOptionEntity.updatedAt shouldBe productOption.updatedAt
    }

    "ProductOptionEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        /* ProductOptionEntity 객체 생성
           - optionId: 옵션 ID
           - name: 옵션 이름
           - additionalPrice: 추가 가격
           - description: 옵션 설명
           - product: 연관된 ProductEntity
           - createdAt: 생성 시간
           - updatedAt: 수정 시간
        */
        val productEntity = ProductEntity(
            productId = 1L,
            title = "테스트 제품",
            price = 1000L,
            typeCode = 1
        )

        val productOptionEntity = ProductOptionEntity(
            optionId = 1L,
            name = "테스트 옵션",
            additionalPrice = 500L,
            description = "옵션 설명",
            product = productEntity,
            createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            updatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        /* ProductOptionEntity에서 도메인 모델로 변환 */
        val productOption = productOptionEntity.toDomain()

        /* 변환된 도메인 모델의 필드 값 확인 */
        productOption.optionId shouldBe productOptionEntity.optionId
        productOption.name shouldBe productOptionEntity.name
        productOption.additionalPrice shouldBe productOptionEntity.additionalPrice
        productOption.description shouldBe productOptionEntity.description
        productOption.productId shouldBe productOptionEntity.product?.productId
        productOption.createdAt shouldBe productOptionEntity.createdAt
        productOption.updatedAt shouldBe productOptionEntity.updatedAt
    }
})
