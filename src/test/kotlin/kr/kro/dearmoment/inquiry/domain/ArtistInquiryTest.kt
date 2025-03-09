package kr.kro.dearmoment.inquiry.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class ArtistInquiryTest : DescribeSpec({
    describe("작가 문의는") {
        context("생성 시 제목이 빈값이면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    StudioInquiry(
                        title = "",
                        content = "content",
                    )
                }
            }
        }

        context("생성 시 내용이 빈값이면 ") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> {
                    StudioInquiry(
                        title = "title",
                        content = "",
                    )
                }
            }
        }
    }
})
