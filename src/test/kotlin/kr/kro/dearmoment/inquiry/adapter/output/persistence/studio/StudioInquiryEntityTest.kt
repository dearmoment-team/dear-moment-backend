package kr.kro.dearmoment.inquiry.adapter.output.persistence.studio

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import java.util.UUID

class StudioInquiryEntityTest : DescribeSpec({
    describe("StudioInquiryEntity") {
        val userId = UUID.randomUUID()
        val title = "Test Inquiry Title"
        val content = "Test Inquiry Content"

        val studioInquiryEntity =
            StudioInquiryEntity(
                userId = userId,
                title = title,
                content = content,
            )

        it("StudioInquiryEntity가 올바르게 생성되어야 한다") {
            studioInquiryEntity.userId shouldBe userId
            studioInquiryEntity.title shouldBe title
            studioInquiryEntity.content shouldBe content
        }

        it("StudioInquiryEntity에서 StudioInquiry 도메인 객체로 변환되어야 한다") {
            val studioInquiry = studioInquiryEntity.toDomain()

            studioInquiry.id shouldBe studioInquiryEntity.id
            studioInquiry.userId shouldBe studioInquiryEntity.userId
            studioInquiry.title shouldBe studioInquiryEntity.title
            studioInquiry.content shouldBe studioInquiryEntity.content
        }

        it("StudioInquiry 도메인 객체에서 StudioInquiryEntity로 변환되어야 한다") {
            val studioInquiry =
                StudioInquiry(
                    id = 1L,
                    userId = userId,
                    title = title,
                    content = content,
                )

            val convertedEntity = StudioInquiryEntity.from(studioInquiry)

            convertedEntity.userId shouldBe studioInquiry.userId
            convertedEntity.title shouldBe studioInquiry.title
            convertedEntity.content shouldBe studioInquiry.content
        }
    }
})
