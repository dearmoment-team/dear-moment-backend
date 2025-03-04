package kr.kro.dearmoment.studio.application.dto.response

import kr.kro.dearmoment.studio.application.dto.StudioPartnerShopDto
import kr.kro.dearmoment.studio.domain.Studio

data class RegisterStudioResponse(
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
    val status: String,
    val partnerShops: List<StudioPartnerShopDto>,
) {
    companion object {
        fun from(domain: Studio) =
            RegisterStudioResponse(
                id = domain.id,
                userId = domain.userId,
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
                            urlLink = it.urlLink,
                        )
                    },
            )
    }
}
