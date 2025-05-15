package kr.kro.dearmoment.product.domain.model.option

import java.time.LocalDateTime

/**
 * 상품 옵션 도메인 모델
 */
data class ProductOption(
    val optionId: Long = 0L,
    val productId: Long,
    val name: String,
    val optionType: OptionType,
    // 할인 구조
    val discountAvailable: Boolean = false,
    val originalPrice: Long = 0,
    val discountPrice: Long = 0,
    val description: String = "",
    // [단품용]
    val costumeCount: Int = 0,
    val shootingLocationCount: Int = 0,
    val shootingHours: Int = 0,
    val shootingMinutes: Int = 0,
    val retouchedCount: Int = 0,
    // [원본 제공 여부]
    val originalProvided: Boolean = true,
    // [패키지용]
    val partnerShops: List<PartnerShop> = emptyList(),
    // 선택 추가사항 필드 (옵션에 따라 필요한 추가 정보)
    val optionalAdditionalDetails: String? = "",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(name.isNotBlank()) { "옵션명은 비어 있을 수 없습니다." }

        require(originalPrice >= 0) { "원 판매가는 0 이상이어야 합니다." }
        require(discountPrice >= 0) { "할인가는 0 이상이어야 합니다." }
        if (discountAvailable) {
            require(discountPrice <= originalPrice) {
                "할인가는 원 판매가보다 클 수 없습니다."
            }
        }

        if (optionType == OptionType.SINGLE) {
            require(shootingLocationCount > 0) {
                "단품 옵션은 촬영 장소 수가 1개 이상이어야 합니다."
            }
            require(shootingHours >= 0 && shootingMinutes >= 0) {
                "단품 옵션 촬영시간은 음수가 될 수 없습니다."
            }
            // ↓ 단순 시+분 합이 아니라 '총 분'으로 계산하도록 수정
            val totalMinutes = shootingHours * 60 + shootingMinutes
            require(totalMinutes >= 1) {
                "단품 옵션의 촬영 시간은 최소 1분 이상이어야 합니다."
            }
        }
        else {
            // PACKAGE
            require(partnerShops.isNotEmpty()) { "패키지 옵션은 1개 이상의 파트너샵이 필요합니다." }
        }
    }

    fun isPriceInRange(
        from: Long,
        to: Long,
    ) = discountPrice in from..to
}
