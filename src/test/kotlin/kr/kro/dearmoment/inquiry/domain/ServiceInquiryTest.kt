package kr.kro.dearmoment.inquiry.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.util.UUID

class ServiceInquiryTest : DescribeSpec({
    describe("서비스 문의는") {
        context("생성 시 제목이 빈값이면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    ServiceInquiry(
                        type = ServiceInquiryType.from("invalid"),
                        content = "content",
                        userId = UUID.randomUUID(),
                    )
                }
            }
        }

        context("생성 시 내용이 빈값이면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    ServiceInquiry(
                        type = ServiceInquiryType.SERVICE_COMPLIMENT,
                        content = "",
                        userId = UUID.randomUUID(),
                    )
                }
            }
        }
    }
})
