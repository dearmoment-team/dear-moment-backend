package kr.kro.dearmoment.studio.adapter.input.dto.response

import kr.kro.dearmoment.studio.adapter.input.dto.StudioPartnerShopDto

data class ModifyStudioResponse(
    val id: Long,
    val name: String,
    val contact: String,
    val studioIntro: String,
    val artistsIntro: String,
    val instagramUrl: String,
    val kakaoChannelUrl: String,
    val reservationNotice: String,
    val cancellationPolicy: String,
    val status: String,
    val partnerShops: List<StudioPartnerShopDto>,
)
