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
import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.WriteAuthorInquiryAnswerCommand
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.UpdateInquiryPort
import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType

class InquiryServiceTest : DescribeSpec({
    val savePort = mockk<SaveInquiryPort>()
    val deletePort = mockk<DeleteInquiryPort>()
    val getPort = mockk<GetInquiryPort>()
    val updatePort = mockk<UpdateInquiryPort>()
    val service = InquiryService(savePort, getPort, updatePort, deletePort)

    describe("createAuthorInquiry()는") {
        context("작가 문의 생성 명령을 전달 받으면") {
            val command = CreateAuthorInquiryCommand(userId = 1L, title = "작가 정보 문의", content = "전화번호 정보가 잘못되었습니다.")
            val expectedId = 1L
            every { savePort.saveAuthorInquiry(any()) } returns expectedId
            it("문의를 저장하고 ID를 반환한다.") {
                val result = service.createAuthorInquiry(command)
                result.inquiryId shouldBe expectedId
                verify(exactly = 1) { savePort.saveAuthorInquiry(any()) }
            }
        }
    }

    describe("createProductInquiry()는") {
        context("상품 문의 생성 명령을 전달 받으면") {
            val command = CreateProductInquiryCommand(userId = 1L, productId = 1L)
            val expectedId = 1L
            every { savePort.saveProductInquiry(any()) } returns expectedId
            it("문의를 저장하고 ID를 반환한다.") {
                val result = service.createProductInquiry(command)
                result.inquiryId shouldBe expectedId
                verify(exactly = 1) { savePort.saveProductInquiry(any()) }
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
                )
            val expectedId = 1L
            every { savePort.saveServiceInquiry(any()) } returns expectedId
            it("문의를 저장하고 ID를 반환한다.") {
                val result = service.createServiceInquiry(command)
                result.inquiryId shouldBe expectedId
                verify(exactly = 1) { savePort.saveServiceInquiry(any()) }
            }
        }

        context("서비스 문의 생성 명령이 유효하지 않으면") {
            val command = CreateServiceInquiryCommand(userId = 1L, type = "invalid type", content = "홈페이지에 접속이 안됩니다..")
            it("에러를 반환한다.") {
                shouldThrow<IllegalArgumentException> { service.createServiceInquiry(command) }
            }
        }
    }

    describe("getAuthorInquiries()는") {
        context("userId가 전달되면") {
            val userId = 1L
            val inquiries =
                listOf(
                    AuthorInquiry(
                        id = 1L,
                        userId = userId,
                        title = "문의1 제목",
                        content = "문의1 내용",
                    ),
                    AuthorInquiry(
                        id = 2L,
                        userId = userId,
                        title = "문의2 제목",
                        content = "문의2 내용",
                    ),
                )

            every { getPort.getAuthorInquiries(userId) } returns inquiries
            it("유저의 작가 문의를 모두 반환한다.") {
                val result = service.getAuthorInquiries(userId)
                result.inquiries.size shouldBe inquiries.size
                verify(exactly = 1) { getPort.getAuthorInquiries(userId) }
            }
        }
    }

    describe("getProductInquiries()는") {
        context("userId가 전달되면") {
            val userId = 1L
            val inquiries =
                listOf(
                    ProductInquiry(
                        id = 1L,
                        userId = userId,
                        productId = 1L,
                        thumbnailUrl = "썸네일 url1",
                    ),
                    ProductInquiry(
                        id = 2L,
                        userId = userId,
                        productId = 2L,
                        thumbnailUrl = "썸네일 url2",
                    ),
                )

            every { getPort.getProductInquiries(userId) } returns inquiries
            it("유저의 작가 문의를 모두 반환한다.") {
                val result = service.getProductInquiries(userId)
                result.inquiries.size shouldBe inquiries.size
                verify(exactly = 1) { getPort.getProductInquiries(userId) }
            }
        }
    }

    describe("writeAuthorInquiryAnswer()는") {
        context("작가 문의 답변 쓰기 명령을 전달 받으면") {
            val inquiryId = 1L
            val answer = "답변 입니다."
            val command = WriteAuthorInquiryAnswerCommand(inquiryId, answer)

            every { updatePort.updateAuthorInquiryAnswer(inquiryId, answer) } returns inquiryId
            it("해당 문의의 답변을 수정한다.") {
                val result = service.writeAuthorInquiryAnswer(command)
                result.inquiryId shouldBe inquiryId
                verify(exactly = 1) { updatePort.updateAuthorInquiryAnswer(inquiryId, answer) }
            }
        }
    }

    describe("removeProductInquiry()는") {
        context("문의 id를 전달받으면") {
            val inquiryId = 1L
            every { deletePort.deleteProductInquiry(inquiryId) } just Runs
            it("해당 문의를 삭제한다") {
                shouldNotThrow<Throwable> { service.removeProductInquiry(inquiryId) }
            }
        }
    }
})
