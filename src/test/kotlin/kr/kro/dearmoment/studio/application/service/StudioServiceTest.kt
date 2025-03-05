package kr.kro.dearmoment.studio.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.command.StudioPartnerShopCommand
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse
import kr.kro.dearmoment.studio.application.port.output.DeleteStudioPort
import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import kr.kro.dearmoment.studio.application.port.output.SaveStudioPort
import kr.kro.dearmoment.studio.application.port.output.UpdateStudioPort
import kr.kro.dearmoment.studio.domain.Studio
import kr.kro.dearmoment.studio.domain.StudioPartnerShopCategory
import kr.kro.dearmoment.studio.domain.StudioStatus

class StudioServiceTest : DescribeSpec({
    val saveStudioPort = mockk<SaveStudioPort>()
    val getStudioPort = mockk<GetStudioPort>()
    val updateStudioPort = mockk<UpdateStudioPort>()
    val deleteStudioPort = mockk<DeleteStudioPort>()
    val service = StudioService(saveStudioPort, getStudioPort, updateStudioPort, deleteStudioPort)

    describe("StudioService 클래스는") {
        context("RegisterStudioCommand를 전달하면") {
            val partnerShopCommand =
                StudioPartnerShopCommand(
                    category = StudioPartnerShopCategory.DRESS.name,
                    name = "드레스 제휴 업체",
                    urlLink = "제휴업체 url link",
                )
            val registerCommand =
                RegisterStudioCommand(
                    userId = 1L,
                    name = "스튜디오 A",
                    contact = "010-1234-5678",
                    studioIntro = "스튜디오 소개글",
                    artistsIntro = "작가 소개글",
                    instagramUrl = "인스타 url",
                    kakaoChannelUrl = "카카오톡 채널 url",
                    reservationNotice = "예약은 평일만 가능합니다.",
                    cancellationPolicy = "환불은 불가능합니다.",
                    status = StudioStatus.ACTIVE.name,
                    partnerShops = listOf(partnerShopCommand),
                )

            val expected =
                Studio(
                    id = 1L,
                    userId = registerCommand.userId,
                    name = registerCommand.name,
                    contact = registerCommand.contact,
                    studioIntro = registerCommand.studioIntro,
                    artistsIntro = registerCommand.artistsIntro,
                    instagramUrl = registerCommand.instagramUrl,
                    kakaoChannelUrl = registerCommand.kakaoChannelUrl,
                    reservationNotice = registerCommand.reservationNotice,
                    cancellationPolicy = registerCommand.cancellationPolicy,
                    status = StudioStatus.from(registerCommand.status),
                    partnerShops = listOf(partnerShopCommand.toDomain()),
                )

            every { saveStudioPort.save(any()) } returns expected
            it("스튜디오를 저장하고 반환한다.") {
                val result = service.register(registerCommand)
                result.id shouldBe expected.id
                verify(exactly = 1) { saveStudioPort.save(any()) }
            }
        }

        context("studioId를 전달하면") {
            val studio =
                Studio(
                    userId = 1L,
                    name = "스튜디오",
                    contact = "010-1234-5678",
                    studioIntro = "스튜디오 소개글",
                    artistsIntro = "작가 소개글",
                    instagramUrl = "인스타 url",
                    kakaoChannelUrl = "카카오톡 채널 url",
                    reservationNotice = "",
                    cancellationPolicy = "",
                    status = StudioStatus.ACTIVE,
                )

            val expected = StudioResponse.from(studio)

            every { getStudioPort.findById(studio.id) } returns studio
            it("studioId에 해당하는 스튜디오를 반환한다.") {
                val result = service.getStudio(studio.id)

                result shouldBe expected
                verify(exactly = 1) { getStudioPort.findById(studio.id) }
            }
        }
        context("ModifyStudioCommand를 전달하면") {
            val partnerShopCommand =
                StudioPartnerShopCommand(
                    category = StudioPartnerShopCategory.DRESS.name,
                    name = "드레스 제휴 업체",
                    urlLink = "제휴업체 url link",
                )
            val modifyCommand =
                ModifyStudioCommand(
                    id = 1L,
                    userId = 1L,
                    name = "스튜디오 A(수정)",
                    contact = "010-1234-5678(수정)",
                    studioIntro = "스튜디오 소개글(수정)",
                    artistsIntro = "작가 소개글(수정)",
                    instagramUrl = "인스타 url(수정)",
                    kakaoChannelUrl = "카카오톡 채널 url(수정)",
                    reservationNotice = "예약은 평일만 가능합니다.(수정)",
                    cancellationPolicy = "환불은 불가능합니다.(수정)",
                    status = StudioStatus.ACTIVE.name,
                    partnerShops = listOf(partnerShopCommand),
                )

            val expected =
                Studio(
                    id = 1L,
                    userId = modifyCommand.userId,
                    name = modifyCommand.name,
                    contact = modifyCommand.contact,
                    studioIntro = modifyCommand.studioIntro,
                    artistsIntro = modifyCommand.artistsIntro,
                    instagramUrl = modifyCommand.instagramUrl,
                    kakaoChannelUrl = modifyCommand.kakaoChannelUrl,
                    reservationNotice = modifyCommand.reservationNotice,
                    cancellationPolicy = modifyCommand.cancellationPolicy,
                    status = StudioStatus.from(modifyCommand.status),
                    partnerShops = listOf(partnerShopCommand.toDomain()),
                )

            every { updateStudioPort.update(any()) } returns expected
            it("스튜디오를 저장하고 반환한다.") {
                val result = service.modify(modifyCommand)
                result.id shouldBe expected.id
                verify(exactly = 1) { updateStudioPort.update(any()) }
            }
        }

        context("스튜디오 식별자를 전달하면") {
            val studioId = 123L

            every { deleteStudioPort.delete(studioId) } just Runs
            it("해당 스튜디오를 삭제한다.") {
                shouldNotThrow<Throwable> { deleteStudioPort.delete(studioId) }
                verify(exactly = 1) { deleteStudioPort.delete(any()) }
            }
        }
    }
})
