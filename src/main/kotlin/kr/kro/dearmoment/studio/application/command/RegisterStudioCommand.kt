package kr.kro.dearmoment.studio.application.command

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.studio.domain.Studio
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

data class RegisterStudioCommand(
    val profileImage: MultipartFile,
    val name: String,
    val userId: UUID,
    val contact: String,
    val studioIntro: String,
    val artistsIntro: String,
    val instagramUrl: String,
    val kakaoChannelUrl: String,
    val reservationNotice: String,
    val cancellationPolicy: String,
    val partnerShops: List<StudioPartnerShopCommand>,
    val status: String,
    val isCasted: Boolean,
) {
    fun toDomain(profileImage: Image) =
        Studio(
            profileImage = profileImage,
            name = name,
            userId = userId,
            contact = contact,
            studioIntro = studioIntro,
            artistsIntro = artistsIntro,
            instagramUrl = instagramUrl,
            kakaoChannelUrl = kakaoChannelUrl,
            reservationNotice = reservationNotice,
            cancellationPolicy = cancellationPolicy,
            partnerShops = partnerShops.map { it.toDomain() },
            status = StudioStatus.from(status),
            isCasted = isCasted,
        )
}
