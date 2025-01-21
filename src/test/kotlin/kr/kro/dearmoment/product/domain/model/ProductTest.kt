package kr.kro.dearmoment.product.domain.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Product 클래스 테스트
 */
internal class ProductTest : StringSpec({

    "typeCode가 1이면 hasPackage는 true" {
        // given
        val product =
            Product(
                productId = 1L,
                userId = 101L,
                title = "패키지 상품",
                description = "테스트 패키지 상품",
                price = 100000,
                typeCode = 1,
            )

        // when
        val result = product.hasPackage

        // then
        result shouldBe true
    }

    "typeCode가 0이면 hasPackage는 false" {
        // given
        val product =
            Product(
                productId = 2L,
                userId = 102L,
                title = "단품 상품",
                description = "테스트 단품 상품",
                price = 50000,
                typeCode = 0,
            )

        // when
        val result = product.hasPackage

        // then
        result shouldBe false
    }

    "options 리스트가 정상적으로 세팅된다" {
        // given
        val optionList =
            listOf(
                ProductOption(
                    optionId = 0L,
                    name = "익스프레스 서비스",
                    additionalPrice = 80000,
                    description = "1주~3주 이내 보정본 전달",
                ),
                ProductOption(
                    optionId = 0L,
                    name = "세부 보정 추가",
                    additionalPrice = 10000,
                    description = "세부 보정 15장",
                ),
            )

        val product =
            Product(
                productId = 3L,
                userId = 103L,
                title = "옵션 포함 상품",
                description = "옵션이 있는 상품",
                price = 150000,
                typeCode = 1,
                options = optionList,
            )

        // then
        product.options?.size shouldBe 2
        product.options?.first()?.name shouldBe "익스프레스 서비스"
        product.options?.last()?.additionalPrice shouldBe 10000
    }
})
