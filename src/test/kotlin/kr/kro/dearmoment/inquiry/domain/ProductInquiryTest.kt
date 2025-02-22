package kr.kro.dearmoment.inquiry.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class ProductInquiryTest : DescribeSpec({
    describe("상품 문의는") {
        context("생성 시 상품 ID가 음수면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    ProductInquiry(
                        productId = -1,
                    )
                }
            }
        }
    }
})
