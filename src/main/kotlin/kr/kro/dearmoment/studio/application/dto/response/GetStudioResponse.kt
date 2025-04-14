package kr.kro.dearmoment.studio.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.studio.application.dto.StudioPartnerShopDto
import kr.kro.dearmoment.studio.domain.Studio

data class GetStudioResponse(
    @Schema(description = "스튜디오 ID", example = "1")
    val id: Long,
    @Schema(description = "스튜디오 이름", example = "디어모먼트 스튜디오")
    val name: String,
    @Schema(description = "연락처", example = "010-1234-5678")
    val contact: String,
    @Schema(description = "스튜디오 소개글", example = "안녕하세요. 디어모먼트 스튜디오 입니다.")
    val studioIntro: String,
    @Schema(description = "작가 소개글", example = "안녕하세요. 디어모먼트 스튜디오의 작가 디모입니다.")
    val artistsIntro: String,
    @Schema(description = "인스타 링크", example = "https://www.instagram.com/username/")
    val instagramUrl: String,
    @Schema(description = "카카오톡 채널 링크", example = "https://pf.kakao.com/adasd/")
    val kakaoChannelUrl: String,
    @Schema(description = "예약 안내", example = "예약은 주중에만 가능합니다.")
    val reservationNotice: String,
    @Schema(description = "취소 및 환불 정책", example = "환불은 불가능합니다.")
    val cancellationPolicy: String,
    @Schema(description = "파트너샵 정보")
    val partnerShops: List<StudioPartnerShopDto>,
    @Schema(
        description = "스튜디오 상태",
        example = "ACTIVE",
        allowableValues = ["ACTIVE, INACTIVE"],
    )
    val status: String,
    @Schema(description = "영입 스튜디오 여부")
    val isCasted: Boolean,
) {
    companion object {
        fun from(domain: Studio) =
            GetStudioResponse(
                id = domain.id,
                name = domain.name,
                contact = domain.contact,
                studioIntro = domain.studioIntro,
                artistsIntro = domain.artistsIntro,
                instagramUrl = domain.instagramUrl,
                kakaoChannelUrl = domain.kakaoChannelUrl,
                reservationNotice = domain.reservationNotice,
                cancellationPolicy = domain.cancellationPolicy,
                status = domain.status.name,
                partnerShops =
                    domain.partnerShops.map {
                        StudioPartnerShopDto(
                            category = it.category.name,
                            name = it.name,
                            urlLink = it.link,
                        )
                    },
                isCasted = domain.isCasted,
            )
    }
}

data class ProductStudioResponse(
    @Schema(description = "스튜디오 이름", example = "디어모먼트 스튜디오")
    val name: String,
    @Schema(description = "연락처", example = "010-1234-5678")
    val contact: String,
    @Schema(description = "스튜디오 소개글", example = "안녕하세요. 디어모먼트 스튜디오 입니다.")
    val studioIntro: String,
    @Schema(description = "작가 소개글", example = "안녕하세요. 디어모먼트 스튜디오의 작가 디모입니다.")
    val artistsIntro: String,
    @Schema(description = "인스타 링크", example = "https://www.instagram.com/username/")
    val instagramUrl: String,
    @Schema(description = "카카오톡 채널 링크", example = "https://pf.kakao.com/adasd/")
    val kakaoChannelUrl: String,
    @Schema(description = "예약 안내", example = "예약은 주중에만 가능합니다.")
    val reservationNotice: String,
    @Schema(description = "취소 및 환불 정책", example = "환불은 불가능합니다.")
    val cancellationPolicy: String,
    @Schema(description = "파트너샵 정보")
    val partnerShops: List<StudioPartnerShopDto>,
) {
    companion object {
        fun from(domain: Studio) =
            ProductStudioResponse(
                name = domain.name,
                contact = domain.contact,
                studioIntro = domain.studioIntro,
                artistsIntro = domain.artistsIntro,
                instagramUrl = domain.instagramUrl,
                kakaoChannelUrl = domain.kakaoChannelUrl,
                reservationNotice = domain.reservationNotice,
                cancellationPolicy = domain.cancellationPolicy,
                partnerShops =
                    domain.partnerShops.map {
                        StudioPartnerShopDto(
                            category = it.category.name,
                            name = it.name,
                            urlLink = it.link,
                        )
                    },
            )
    }
}
