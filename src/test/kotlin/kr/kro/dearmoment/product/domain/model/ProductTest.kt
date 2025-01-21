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
                description = "테스트 패키지 상품입니다.",
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
                description = "테스트 단품 상품입니다.",
                price = 50000,
                typeCode = 0,
            )

        // when
        val result = product.hasPackage

        // then
        result shouldBe false
    }
})
