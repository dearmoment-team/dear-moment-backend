package kr.kro.dearmoment.studio.domain

import kr.kro.dearmoment.product.domain.model.Product

/***
 * TODO: 유저 연동 필요
 */
data class Studio(
    val id: Long = 0L,
    val userId: Long,
    val name: String,
    val contact: String,
    val studioIntro: String,
    val artistsIntro: String,
    val instagramUrl: String,
    val kakaoChannelUrl: String,
    val reservationNotice: String,
    val cancellationPolicy: String,
    val status: StudioStatus,
    val partnerShops: List<StudioPartnerShop> = emptyList(),
    val products: List<Product> = emptyList(),
) {
    init {
        require(name.isNotEmpty()) { "스튜디오 이름은 필수 입니다." }
        require(contact.isNotEmpty()) { "연락처는 필수 입니다." }
        require(studioIntro.isNotEmpty()) { "스튜디오 소개글은 필수 입니다." }
        require(artistsIntro.isNotEmpty()) { "작가 소개글은 필수 입니다." }
        require(instagramUrl.isNotEmpty()) { "인스타그램 링크는 필수 입니다." }
        require(kakaoChannelUrl.isNotEmpty()) { "카카오톡 채널 링크는 필수 입니다." }
    }
}
