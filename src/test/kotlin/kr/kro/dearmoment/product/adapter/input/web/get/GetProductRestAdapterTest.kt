package kr.kro.dearmoment.product.adapter.input.web.get

import andDocument
import io.mockk.every
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.response.GetProductResponse
import kr.kro.dearmoment.product.application.dto.response.ImageResponse
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.studio.application.dto.StudioPartnerShopDto
import kr.kro.dearmoment.studio.application.dto.response.ProductStudioResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class GetProductRestAdapterTest : RestApiTestBase() {
    /**
     * 1) 상품 단건 조회 - 정상 조회
     */
    @Test
    fun `상품 단건 조회 API 테스트 - 정상 조회`() {
        // given
        val userId = UUID.randomUUID() // 가정: 인증된 사용자 ID
        val productResponse =
            GetProductResponse(
                productId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "New Product",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("MODERN"),
                mainImage = ImageResponse(imageId = 1L, url = "http://image-server.com/mainImage.jpg"),
                subImages =
                    listOf(
                        ImageResponse(imageId = 2L, url = "http://image-server.com/subImage1.jpg"),
                        ImageResponse(imageId = 3L, url = "http://image-server.com/subImage2.jpg"),
                        ImageResponse(imageId = 4L, url = "http://image-server.com/subImage3.jpg"),
                        ImageResponse(imageId = 5L, url = "http://image-server.com/subImage4.jpg"),
                    ),
                additionalImages = emptyList(),
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
                studio =
                    ProductStudioResponse(
                        name = "디어모먼트 스튜디오",
                        contact = "010-1234-5678",
                        studioIntro = "스튜디오 소개글",
                        artistsIntro = "작가 소개글",
                        instagramUrl = "https://www.instagram.com/username/",
                        kakaoChannelUrl = "http://pf.kakao.com/user",
                        reservationNotice = "예약은 평일만 가능합니다.",
                        cancellationPolicy = "환불은 불가능합니다.",
                        partnerShops =
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
                            ),
                    ),
                likeId = 0L,
            )
        // UseCase stubbing
        every { getProductUseCase.getProductById(1L, userId) } returns productResponse

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)

        // 인증 정보 추가 (withAuthenticatedUser는 RestApiTestBase 등에 정의된 메서드라고 가정)
        val authenticatedRequest = withAuthenticatedUser(userId, requestBuilder)

        // then
        mockMvc.perform(authenticatedRequest)
            .andExpect(status().isOk)
            .andDocument(
                "get-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.likeId" type NUMBER means "상품 좋아요 ID",
                    "data.userId" type STRING means "사용자 ID (null일 수 있음)",
                    "data.productType" type STRING means "상품 유형",
                    "data.shootingPlace" type STRING means "촬영 장소",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data.cameraTypes" type ARRAY means "카메라 종류 목록",
                    "data.retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.mainImage" type OBJECT means "대표 이미지",
                    "data.mainImage.imageId" type NUMBER means "대표 이미지 ID",
                    "data.mainImage.url" type STRING means "대표 이미지 URL",
                    "data.subImages" type ARRAY means "서브 이미지 목록",
                    "data.subImages[].imageId" type NUMBER means "서브 이미지 ID",
                    "data.subImages[].url" type STRING means "서브 이미지 URL",
                    "data.additionalImages" type ARRAY means "추가 이미지 목록",
                    "data.additionalImages[].imageId" type NUMBER means "추가 이미지 ID",
                    "data.additionalImages[].url" type STRING means "추가 이미지 URL",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.studio" type OBJECT means "스튜디오 정보",
                    "data.studio.name" type STRING means "스튜디오 이름",
                    "data.studio.contact" type STRING means "스튜디오 연락처",
                    "data.studio.studioIntro" type STRING means "스튜디오 소개",
                    "data.studio.artistsIntro" type STRING means "스튜디오 작가 소개",
                    "data.studio.instagramUrl" type STRING means "스튜디오 인스타 링크",
                    "data.studio.kakaoChannelUrl" type STRING means "스튜디오 카카오톡 채널 링크",
                    "data.studio.reservationNotice" type STRING means "스튜디오 예약 안내",
                    "data.studio.cancellationPolicy" type STRING means "스튜디오 환불 정책",
                    "data.studio.partnerShops" type ARRAY means "스튜디오 제휴 업체",
                    "data.studio.partnerShops[].category" type STRING means "제휴 업체 카테고리",
                    "data.studio.partnerShops[].name" type STRING means "제휴 업체 이름",
                    "data.studio.partnerShops[].urlLink" type STRING means "제휴 업체 링크",
                ),
            )
    }

    /**
     * 2) 상품 단건 조회 - 존재하지 않는 상품
     */
    @Test
    fun `상품 단건 조회 API 테스트 - 존재하지 않는 상품`() {
        // given
        val userId = UUID.randomUUID()
        every { getProductUseCase.getProductById(999L, userId) } throws
            IllegalArgumentException("Product with ID 999 not found.")

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)

        // 인증 정보 추가
        val authenticatedRequest = withAuthenticatedUser(userId, requestBuilder)

        // then
        mockMvc.perform(authenticatedRequest)
            .andExpect(status().isBadRequest)
            .andDocument(
                "get-product-not-found",
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }

    /**
     * 3) 내 상품 조회 - 정상 조회
     */
    @Test
    fun `내 상품 상세 조회 API 테스트 - 정상 조회`() {
        // given
        val userId = UUID.randomUUID() // 임의의 인증 사용자
        val myProductResponse =
            GetProductResponse(
                productId = 10L,
                productType = "WEDDING_SNAP",
                shootingPlace = "BUSAN",
                title = "My Product",
                availableSeasons = listOf("YEAR_2026_FIRST_HALF"),
                cameraTypes = listOf("FILM"),
                retouchStyles = listOf("NATURAL"),
                mainImage = ImageResponse(imageId = 100L, url = "http://image-server.com/myMain.jpg"),
                subImages =
                    listOf(
                        ImageResponse(imageId = 101L, url = "http://image-server.com/mySub1.jpg"),
                        ImageResponse(imageId = 102L, url = "http://image-server.com/mySub2.jpg"),
                    ),
                additionalImages = emptyList(),
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
                studio =
                    ProductStudioResponse(
                        name = "내 스튜디오",
                        contact = "010-2222-3333",
                        studioIntro = "내 스튜디오 소개글",
                        artistsIntro = "내 작가 소개",
                        instagramUrl = "https://www.instagram.com/mystudio",
                        kakaoChannelUrl = "https://pf.kakao.com/mystudio",
                        reservationNotice = "주말만 가능합니다.",
                        cancellationPolicy = "취소 불가.",
                        partnerShops = emptyList(),
                    ),
                likeId = 0L,
            )

        every { getProductUseCase.getMyProduct(userId) } returns listOf(myProductResponse)

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/mine")
                .contentType(MediaType.APPLICATION_JSON)

        // 인증 정보 추가
        val authenticatedRequest = withAuthenticatedUser(userId, requestBuilder)

        // then
        mockMvc.perform(authenticatedRequest)
            .andExpect(status().isOk)
            .andDocument(
                "get-my-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type ARRAY means "내 상품 데이터 목록",
                    "data[].likeId" type NUMBER means "좋아요 ID (0이면 좋아요 없음)",
                    "data[].productId" type NUMBER means "내 상품 ID",
                    "data[].productType" type STRING means "상품 유형",
                    "data[].shootingPlace" type STRING means "촬영 장소",
                    "data[].title" type STRING means "상품명",
                    "data[].description" type STRING means "상품 설명",
                    "data[].availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data[].cameraTypes" type ARRAY means "카메라 종류 목록",
                    "data[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data[].mainImage" type OBJECT means "대표 이미지",
                    "data[].mainImage.imageId" type NUMBER means "대표 이미지 ID",
                    "data[].mainImage.url" type STRING means "대표 이미지 URL",
                    "data[].subImages" type ARRAY means "서브 이미지 목록",
                    "data[].subImages[].imageId" type NUMBER means "서브 이미지 ID",
                    "data[].subImages[].url" type STRING means "서브 이미지 URL",
                    "data[].additionalImages" type ARRAY means "추가 이미지 목록",
                    "data[].detailedInfo" type STRING means "상세 정보",
                    "data[].contactInfo" type STRING means "연락처",
                    "data[].createdAt" type OBJECT means "생성 시간",
                    "data[].updatedAt" type OBJECT means "수정 시간",
                    "data[].options" type ARRAY means "옵션 목록",
                    "data[].studio" type OBJECT means "스튜디오 정보",
                    "data[].studio.name" type STRING means "스튜디오 이름",
                    "data[].studio.contact" type STRING means "스튜디오 연락처",
                    "data[].studio.studioIntro" type STRING means "스튜디오 소개",
                    "data[].studio.artistsIntro" type STRING means "스튜디오 작가 소개",
                    "data[].studio.instagramUrl" type STRING means "인스타그램 URL",
                    "data[].studio.kakaoChannelUrl" type STRING means "카카오채널 URL",
                    "data[].studio.reservationNotice" type STRING means "예약 안내",
                    "data[].studio.cancellationPolicy" type STRING means "취소·환불 정책",
                    "data[].studio.partnerShops" type ARRAY means "제휴 업체 목록",
                ),
            )
    }

    /**
     * 4) 내 상품 조회 - 존재하지 않는 상품
     */
    @Test
    fun `내 상품 상세 조회 API 테스트 - 존재하지 않는 상품`() {
        // given
        val userId = UUID.randomUUID()
        every { getProductUseCase.getMyProduct(userId) } throws
            IllegalArgumentException("No product found for user $userId")

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/mine")
                .contentType(MediaType.APPLICATION_JSON)

        // 인증 정보 추가
        val authenticatedRequest = withAuthenticatedUser(userId, requestBuilder)

        // then
        mockMvc.perform(authenticatedRequest)
            .andExpect(status().isBadRequest)
            .andDocument(
                "get-my-product-not-found",
                responseBody(
                    "message" type STRING means "오류 메시지",
                ),
            )
    }
}
