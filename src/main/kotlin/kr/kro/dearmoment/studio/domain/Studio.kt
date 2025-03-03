package kr.kro.dearmoment.studio.domain

import kr.kro.dearmoment.product.domain.model.Product

/***
 * TODO: 유저 연동 필요
 */
data class Studio(
    val id: Long = 0L, // 스튜디오 ID
    val name: String, // 스튜디오명
    val contact: String, // 연락처
    val studioIntro: String, // 스튜디오 소개글
    val artistsIntro: String, // 소속 작가 소개글
    val instagramUrl: String, // 인스타그램 링크
    val kakaoChannelUrl: String, // 카카오톡 채널 링크
    val reservationNotice: String, // 예약 전 안내사항
    val cancellationPolicy: String, // 취소 및 환불 규정
    val status: StudioStatus, // 상태 (활성/비활성)
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
