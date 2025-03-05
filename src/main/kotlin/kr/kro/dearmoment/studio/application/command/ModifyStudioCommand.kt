package kr.kro.dearmoment.studio.application.command

import kr.kro.dearmoment.studio.domain.Studio
import kr.kro.dearmoment.studio.domain.StudioStatus

data class ModifyStudioCommand(
    val id: Long,
    val userId: Long,
    val name: String,
    val contact: String,
    val studioIntro: String,
    val artistsIntro: String,
    val instagramUrl: String,
    val kakaoChannelUrl: String,
    val reservationNotice: String,
    val cancellationPolicy: String,
    val partnerShops: List<StudioPartnerShopCommand>,
    val status: String,
) {
    fun toDomain() =
        Studio(
            id = id,
            userId = userId,
            name = name,
            contact = contact,
            studioIntro = studioIntro,
            artistsIntro = artistsIntro,
            instagramUrl = instagramUrl,
            kakaoChannelUrl = kakaoChannelUrl,
            reservationNotice = reservationNotice,
            cancellationPolicy = cancellationPolicy,
            partnerShops = partnerShops.map { it.toDomain() },
            status = StudioStatus.from(status),
        )
}
