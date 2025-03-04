package kr.kro.dearmoment.studio.application.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.dto.StudioPartnerShopDto

data class RegisterStudioRequest(
    @field:NotNull(message = "유저 ID는 필수입니다.")
    val userId: Long,
    @field:NotBlank(message = "스튜디오 이름은 필수입니다.")
    val name: String,
    @field:Pattern(
        regexp = "^010-\\d{4}-\\d{4}$",
        message = "연락처는 010-1234-5678 형식이어야 합니다.",
    )
    val contact: String,
    @field:Max(value = 100L, message = "스튜디오 소개글은 100자 이하여야 합니다.")
    @field:NotBlank(message = "스튜디오 소개글은 필수입니다.")
    val studioIntro: String,
    @field:NotBlank(message = "작가 소개글은 필수입니다.")
    val artistsIntro: String,
    @field:NotBlank(message = "인스타 링크는 필수입니다.")
    val instagramUrl: String,
    @field:NotBlank(message = "카카오톡 채널 링크는 필수입니다.")
    val kakaoChannelUrl: String,
    val reservationNotice: String,
    val cancellationPolicy: String,
    val partnerShops: List<StudioPartnerShopDto>,
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
