package kr.kro.dearmoment.inquiry.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.util.UUID

class ProductOptionInquiryTest : DescribeSpec({
    describe("상품 문의는") {
        context("생성 시 상품 ID가 음수면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    CreateProductOptionInquiry(
                        productId = -1,
                        optionId = 1L,
                        userId = UUID.randomUUID(),
                    )
                }
            }
        }
        context("생성 시 옵션 ID가 음수면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    CreateProductOptionInquiry(
                        productId = 1L,
                        optionId = -1L,
                        userId = UUID.randomUUID(),
                    )
                }
            }
        }
    }
})
