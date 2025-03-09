package kr.kro.dearmoment.studio.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.dto.StudioPartnerShopDto

data class RegisterStudioRequest(
    @Schema(description = "사용자 ID", example = "1", required = true)
    @field:NotNull(message = "유저 ID는 필수입니다.")
    val userId: Long,
    @Schema(description = "스튜디오 이름", example = "디어모먼트 스튜디오", required = true)
    @field:NotBlank(message = "스튜디오 이름은 필수입니다.")
    val name: String,
    @Schema(description = "연락처", example = "010-1234-5678", required = true)
    @field:Pattern(
        regexp = "^010-\\d{4}-\\d{4}$",
        message = "연락처는 010-1234-5678 형식이어야 합니다.",
    )
    val contact: String,
    @Schema(description = "스튜디오 소개글", example = "안녕하세요. 디어모먼트 스튜디오 입니다.", required = true)
    @field:Max(value = 100L, message = "스튜디오 소개글은 100자 이하여야 합니다.")
    @field:NotBlank(message = "스튜디오 소개글은 필수입니다.")
    val studioIntro: String,
    @Schema(description = "작가 소개글", example = "안녕하세요. 디어모먼트 스튜디오의 작가 디모입니다.", required = true)
    @field:NotBlank(message = "작가 소개글은 필수입니다.")
    val artistsIntro: String,
    @Schema(description = "인스타 링크", example = "https://www.instagram.com/username/", required = true)
    @field:NotBlank(message = "인스타 링크는 필수입니다.")
    val instagramUrl: String,
    @Schema(description = "카카오톡 채널 링크", example = "https://pf.kakao.com/adasd/", required = true)
    @field:NotBlank(message = "카카오톡 채널 링크는 필수입니다.")
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
        required = true,
    )
    val status: String,
) {
    fun toCommand() =
        RegisterStudioCommand(
            name = name,
            userId = userId,
            contact = contact,
            studioIntro = studioIntro,
            artistsIntro = artistsIntro,
            instagramUrl = instagramUrl,
            kakaoChannelUrl = kakaoChannelUrl,
            reservationNotice = reservationNotice,
            cancellationPolicy = cancellationPolicy,
            partnerShops = partnerShops.map { it.toCommand() },
            status = status,
        )
}
