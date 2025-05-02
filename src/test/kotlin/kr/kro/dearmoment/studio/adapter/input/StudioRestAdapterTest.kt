package kr.kro.dearmoment.studio.adapter.input

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.means
import kr.kro.dearmoment.common.restdocs.pathParameters
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.requestParts
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.studio.application.dto.StudioPartnerShopDto
import kr.kro.dearmoment.studio.application.dto.request.ModifyStudioRequest
import kr.kro.dearmoment.studio.application.dto.request.RegisterStudioRequest
import kr.kro.dearmoment.studio.application.dto.response.GetStudioResponse
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

class StudioRestAdapterTest : RestApiTestBase() {
    @Test
    fun `스튜디오 등록(생성) API`() {
        val partnerShopsDto =
            listOf(
                StudioPartnerShopDto(
                    category = PartnerShopCategory.DRESS.name,
                    name = "디어모먼트 드레스샵",
                    urlLink = "dear-moment-dress-shop.partner-shop.url",
                ),
                StudioPartnerShopDto(
                    category = PartnerShopCategory.MENS_SUIT.name,
                    name = "디어모먼트 남자 수트샵",
                    urlLink = "dear-moment-mens-suit.partner-shop.url",
                ),
            )
        val requestBody =
            RegisterStudioRequest(
                name = "디어모먼트 스튜디오",
                contact = "010-1234-5678",
                studioIntro = "스튜디오 소개글",
                artistsIntro = "작가 소개글",
                instagramUrl = "https://www.instagram.com/username/",
                kakaoChannelUrl = "http://pf.kakao.com/user",
                reservationNotice = "예약은 평일만 가능합니다.",
                cancellationPolicy = "환불은 불가능합니다.",
                status = StudioStatus.ACTIVE.name,
                partnerShops = partnerShopsDto,
                isCasted = false,
            )

        val requestPart =
            MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                requestBody.toJsonString().toByteArray(),
            )

        val profileImage =
            MockMultipartFile(
                "profileImageFile",
                "profileImage.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "profile image content".toByteArray(),
            )

        val expected =
            StudioResponse(id = 1L)

        every { registerStudioUseCase.register(requestBody.toCommand(userId, profileImage)) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .multipart("/api/studios")
                .file(requestPart)
                .file(profileImage)
                .contentType(MediaType.MULTIPART_FORM_DATA)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "register-studio",
                requestParts(
                    "request",
                    "userId" type NUMBER means "유저 ID",
                    "isCasted" type BOOLEAN means "영입 여부",
                    "name" type STRING means "스튜디오 이름",
                    "contact" type STRING means "연락처",
                    "studioIntro" type STRING means "스튜디오 소개",
                    "artistsIntro" type STRING means "작가 소개",
                    "instagramUrl" type STRING means "인스타그램 URL",
                    "kakaoChannelUrl" type STRING means "카카오 채널 URL",
                    "reservationNotice" type STRING means "예약 안내",
                    "cancellationPolicy" type STRING means "취소 및 환불 정책",
                    "status" type STRING means "스튜디오 상태 (ACTIVE, INACTIVE)",
                    "partnerShops" type ARRAY means "제휴 업체 목록",
                    "partnerShops[].category" type STRING means "제휴 업체 구분 " +
                        "(HAIR_MAKEUP, DRESS, MENS_SUIT, BOUQUET, VIDEO, STUDIO, ETC)",
                    "partnerShops[].name" type STRING means "제휴 업체 이름",
                    "partnerShops[].urlLink" type STRING means "제휴 업체 URL",
                ),
                responseBody(
                    "data" type OBJECT means "응답 데이터",
                    "data.id" type NUMBER means "등록된 스튜디오 ID",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun`스튜디오 조회 API`() {
        val existedStudioId = 1L
        val partnerShopsDto =
            listOf(
                StudioPartnerShopDto(
                    category = PartnerShopCategory.DRESS.name,
                    name = "디어모먼트 드레스샵(수정)",
                    urlLink = "dear-moment-dress-shop.partner-shop-modify.url",
                ),
                StudioPartnerShopDto(
                    category = PartnerShopCategory.MENS_SUIT.name,
                    name = "디어모먼트 남자 수트샵(수정)",
                    urlLink = "dear-moment-mens-suit.partner-shop-modify.url",
                ),
            )

        val expected =
            GetStudioResponse(
                id = existedStudioId,
                name = "디어모먼트 스튜디오(수정)",
                contact = "010-1111-2222",
                studioIntro = "스튜디오 소개글(수정)",
                artistsIntro = "작가 소개글(수정)",
                instagramUrl = "https://www.instagram.com/username-modify",
                kakaoChannelUrl = "http://pf.kakao.com/user-modify",
                reservationNotice = "예약은 공휴일 제외 가능합니다.(수정)",
                cancellationPolicy = "환불은 가능합니다.(수정)",
                status = StudioStatus.ACTIVE.name,
                partnerShops = partnerShopsDto,
                isCasted = true,
                profileImageUrl = "image.url.link.com"
            )

        every { getStudioUseCase.getStudio(existedStudioId) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .get("/api/studios/{studioId}", existedStudioId)
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-studio",
                pathParameters("studioId" means "조회할 스튜디오 id"),
                responseBody(
                    "data" type OBJECT means "응답 데이터",
                    "data.id" type NUMBER means "등록된 스튜디오 ID",
                    "data.userId" type NUMBER means "유저 ID",
                    "data.isCasted" type BOOLEAN means "영입 여부",
                    "data.profileImageUrl" type STRING means "프로필 이미지 url",
                    "data.name" type STRING means "스튜디오 이름",
                    "data.contact" type STRING means "연락처",
                    "data.studioIntro" type STRING means "스튜디오 소개",
                    "data.artistsIntro" type STRING means "작가 소개",
                    "data.instagramUrl" type STRING means "인스타그램 URL",
                    "data.kakaoChannelUrl" type STRING means "카카오 채널 URL",
                    "data.reservationNotice" type STRING means "예약 안내",
                    "data.cancellationPolicy" type STRING means "취소 및 환불 정책",
                    "data.status" type STRING means "스튜디오 상태 (ACTIVE, INACTIVE)",
                    "data.partnerShops" type ARRAY means "제휴 업체 목록",
                    "data.partnerShops[].category" type STRING means "제휴 업체 구분 " +
                        "(HAIR_MAKEUP, DRESS, MENS_SUIT, BOUQUET, VIDEO, STUDIO, ETC)",
                    "data.partnerShops[].name" type STRING means "제휴 업체 이름",
                    "data.partnerShops[].urlLink" type STRING means "제휴 업체 링크",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `스튜디오 수정 API`() {
        val existedStudioId = 1L
        val partnerShopsDto =
            listOf(
                StudioPartnerShopDto(
                    category = PartnerShopCategory.DRESS.name,
                    name = "디어모먼트 드레스샵(수정)",
                    urlLink = "dear-moment-dress-shop.partner-shop-modify.url",
                ),
                StudioPartnerShopDto(
                    category = PartnerShopCategory.MENS_SUIT.name,
                    name = "디어모먼트 남자 수트샵(수정)",
                    urlLink = "dear-moment-mens-suit.partner-shop-modify.url",
                ),
            )
        val requestBody =
            ModifyStudioRequest(
                name = "디어모먼트 스튜디오(수정)",
                contact = "010-1111-2222",
                studioIntro = "스튜디오 소개글(수정)",
                artistsIntro = "작가 소개글(수정)",
                instagramUrl = "https://www.instagram.com/username-modify",
                kakaoChannelUrl = "http://pf.kakao.com/user-modify",
                reservationNotice = "예약은 공휴일 제외 가능합니다.(수정)",
                cancellationPolicy = "환불은 가능합니다.(수정)",
                status = StudioStatus.ACTIVE.name,
                partnerShops = partnerShopsDto,
                isCasted = false,
            )

        val requestPart =
            MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                requestBody.toJsonString().toByteArray(),
            )

        val profileImage =
            MockMultipartFile(
                "profileImageFile",
                "profileImage.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "profile image content".toByteArray(),
            )

        val expected =
            StudioResponse(id = existedStudioId)

        every { modifyStudioUseCase.modify(requestBody.toCommand(existedStudioId, profileImage, userId)) } returns expected

        val request =
            RestDocumentationRequestBuilders
                .multipart("/api/studios/{studioId}", existedStudioId)
                .file(requestPart)
                .file(profileImage)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with {
                    it.method = "PUT"
                    it
                }

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "modify-studio",
                pathParameters("studioId" means "수정할 스튜디오 id"),
                requestParts(
                    "request",
                    "userId" type NUMBER means "유저 ID",
                    "name" type STRING means "스튜디오 이름",
                    "contact" type STRING means "연락처",
                    "studioIntro" type STRING means "스튜디오 소개",
                    "artistsIntro" type STRING means "작가 소개",
                    "instagramUrl" type STRING means "인스타그램 URL",
                    "kakaoChannelUrl" type STRING means "카카오 채널 URL",
                    "reservationNotice" type STRING means "예약 안내",
                    "cancellationPolicy" type STRING means "취소 및 환불 정책",
                    "status" type STRING means "스튜디오 상태 (ACTIVE, INACTIVE)",
                    "partnerShops" type ARRAY means "제휴 업체 목록",
                    "partnerShops[].category" type STRING means "제휴 업체 구분 " +
                        "(HAIR_MAKEUP, DRESS, MENS_SUIT, BOUQUET, VIDEO, STUDIO, ETC)",
                    "partnerShops[].name" type STRING means "제휴 업체 이름",
                    "partnerShops[].urlLink" type STRING means "제휴 업체 URL",
                    "isCasted" type BOOLEAN means "영입 여부",
                ),
                responseBody(
                    "data" type OBJECT means "응답 데이터",
                    "data.id" type NUMBER means "등록된 스튜디오 ID",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `스튜디오 삭제 API`() {
        val studioId = 1L

        every { deleteStudioUseCase.delete(studioId) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/studios/{studioId}", studioId)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-studio",
                pathParameters("studioId" means "삭제할 스튜디오 ID"),
            )
    }
}
