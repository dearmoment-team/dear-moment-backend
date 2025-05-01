package kr.kro.dearmoment.studio.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.fixture.imageFixture
import kr.kro.dearmoment.common.fixture.studioFixture
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.command.StudioPartnerShopCommand
import kr.kro.dearmoment.studio.application.dto.response.GetStudioResponse
import kr.kro.dearmoment.studio.application.port.output.DeleteStudioPort
import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import kr.kro.dearmoment.studio.application.port.output.SaveStudioPort
import kr.kro.dearmoment.studio.application.port.output.UpdateStudioPort
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.util.UUID

class StudioServiceTest : DescribeSpec({
    val saveStudioPort = mockk<SaveStudioPort>()
    val getStudioPort = mockk<GetStudioPort>()
    val updateStudioPort = mockk<UpdateStudioPort>()
    val deleteStudioPort = mockk<DeleteStudioPort>()
    val imageService = mockk<ImageService>()
    val service = StudioService(saveStudioPort, getStudioPort, updateStudioPort, deleteStudioPort, imageService)

    describe("StudioService 클래스는") {
        context("RegisterStudioCommand를 전달하면") {
            val profileImage =
                MockMultipartFile(
                    "profileImageFile",
                    "profileImage.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "profile image content".toByteArray(),
                )
            val partnerShopCommand =
                StudioPartnerShopCommand(
                    category = PartnerShopCategory.DRESS.name,
                    name = "드레스 제휴 업체",
                    urlLink = "제휴업체 url link",
                )
            val registerCommand =
                RegisterStudioCommand(
                    userId = UUID.randomUUID(),
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
                    isCasted = true,
                    profileImage = profileImage,
                )

            val image = imageFixture(registerCommand.userId)
            val expected = registerCommand.toDomain(image)

            every { saveStudioPort.save(any()) } returns expected
            every { imageService.save(any()) } returns image
            it("스튜디오를 저장하고 반환한다.") {
                val result = service.register(registerCommand)
                result.id shouldBe expected.id
                verify(exactly = 1) { saveStudioPort.save(any()) }
                verify(exactly = 1) { imageService.save(any()) }
            }
        }

        context("studioId를 전달하면") {
            val studio = studioFixture()
            val expected = GetStudioResponse.from(studio)

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
                    category = PartnerShopCategory.DRESS.name,
                    name = "드레스 제휴 업체",
                    urlLink = "제휴업체 url link",
                )
            val modifyCommand =
                ModifyStudioCommand(
                    id = 1L,
                    userId = UUID.randomUUID(),
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
                    isCasted = true,
                    profileImage =
                        MockMultipartFile(
                            "profileImageFile",
                            "profileImage.jpg",
                            MediaType.IMAGE_JPEG_VALUE,
                            "profile image content".toByteArray(),
                        )
                )

            val updateImage = imageFixture(modifyCommand.userId)
            val expected = modifyCommand.toDomain(updateImage)

            every { getStudioPort.findById(any()) } returns expected
            every { updateStudioPort.update(any()) } returns expected
            every { imageService.delete(any()) } just Runs
            every { imageService.save(any()) } returns updateImage

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
