package kr.kro.dearmoment.inquiry.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateStudioInquiryCommand
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SendInquiryPort
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType

class InquiryCommandServiceTest : DescribeSpec({
    val savePort = mockk<SaveInquiryPort>()
    val deletePort = mockk<DeleteInquiryPort>()
    val sendPort = mockk<SendInquiryPort>()
    val service = InquiryCommandService(savePort, deletePort, sendPort)

    describe("createArtistInquiry()는") {
        context("작가 문의 생성 명령을 전달 받으면") {
            val command =
                CreateStudioInquiryCommand(
                    userId = 1L,
                    title = "작가 정보 문의",
                    content = "전화번호 정보가 잘못되었습니다.",
                    email = "email@email.com",
                )
            val expectedId = 1L
            every { savePort.saveStudioInquiry(any()) } returns expectedId
            every { sendPort.sendMail(any(), any(), any()) } just Runs
            it("문의를 저장하고 ID를 반환한다.") {
                val result = service.createStudioInquiry(command)
                result.inquiryId shouldBe expectedId
                verify(exactly = 1) { savePort.saveStudioInquiry(any()) }
            }
        }
    }

    describe("createProductInquiry()는") {
        context("상품 문의 생성 명령을 전달 받으면") {
            val command = CreateProductInquiryCommand(userId = 1L, productId = 1L)
            val expectedId = 1L
            every { savePort.saveProductOptionInquiry(any()) } returns expectedId
            it("문의를 저장하고 ID를 반환한다.") {
                val result = service.createProductInquiry(command)
                result.inquiryId shouldBe expectedId
                verify(exactly = 1) { savePort.saveProductOptionInquiry(any()) }
            }
        }
    }

    describe("createServiceInquiry()는") {
        context("서비스 문의 생성 명령을 전달 받으면") {
            val command =
                CreateServiceInquiryCommand(
                    userId = 1L,
                    type = ServiceInquiryType.SYSTEM_ERROR_REPORT.name,
                    content = "홈페이지에 접속이 안됩니다..",
                    email = "email@email.com",
                )
            val expectedId = 1L
            every { savePort.saveServiceInquiry(any()) } returns expectedId
            every { sendPort.sendMail(any(), any(), any()) } just Runs
            it("문의를 저장하고 ID를 반환한다.") {
                val result = service.createServiceInquiry(command)
                result.inquiryId shouldBe expectedId
                verify(exactly = 1) { savePort.saveServiceInquiry(any()) }
            }
        }

        context("서비스 문의 생성 명령이 유효하지 않으면") {
            val command =
                CreateServiceInquiryCommand(
                    userId = 1L,
                    type = "invalid type",
                    content = "홈페이지에 접속이 안됩니다..",
                    email = "email@email.com",
                )
            it("에러를 반환한다.") {
                shouldThrow<IllegalArgumentException> { service.createServiceInquiry(command) }
            }
        }
    }

    describe("removeProductInquiry()는") {
        context("문의 id를 전달받으면") {
            val inquiryId = 1L
            every { deletePort.deleteProductOptionInquiry(inquiryId) } just Runs
            it("해당 문의를 삭제한다") {
                shouldNotThrow<Throwable> { service.removeProductOptionInquiry(inquiryId) }
            }
        }
    }
})
