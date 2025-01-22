package kr.kro.dearmoment.product.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Product 클래스 테스트
 */
internal class ProductTest : StringSpec({
    "상품 생성 시 hasPackage가 true여야 한다" {
        // given
        val product =
            Product(
                productId = 1L,
                userId = 1L,
                title = "테스트 상품",
                description = "테스트 설명",
                price = 100000,
                typeCode = 1,
            )

        // when
        val result = product.hasPackage

        // then
        result shouldBe true
    }

    "typeCode가 0인 상품은 hasPackage가 false여야 한다" {
        // given
        val product =
            Product(
                productId = 2L,
                userId = 2L,
                title = "일반 상품",
                description = "일반 설명",
                price = 50000,
                typeCode = 0,
            )

        // when
        val result = product.hasPackage

        // then
        result shouldBe false
    }

    "options가 제공되지 않은 경우 빈 리스트여야 한다" {
        // given
        val product =
            Product(
                productId = 3L,
                userId = 3L,
                title = "옵션 없는 상품",
                description = "옵션 없음 설명",
                price = 75000,
                typeCode = 1,
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
                    name = "옵션1",
                    additionalPrice = 10000,
                    description = "옵션 설명1",
                ),
                ProductOption(
                    optionId = 2L,
                    name = "옵션2",
                    additionalPrice = 20000,
                    description = "옵션 설명2",
                ),
            )

        val product =
            Product(
                productId = 4L,
                userId = 4L,
                title = "옵션 있는 상품",
                description = "옵션 있음 설명",
                price = 150000,
                typeCode = 1,
                options = options,
            )

        // when
        val result = product.options

        // then
        result shouldBe options
    }
})
