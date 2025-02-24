package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

/**
 * 옵션의 타입(단품 or 패키지)
 */
enum class OptionType {
    SINGLE,    // 단품
    PACKAGE    // 패키지
}

/**
 * 패키지의 종류 (헤어/메이크업, 드레스, 드론, DVD 등)
 */
enum class PackageCategory {
    HAIR_MAKEUP,
    DRESS,
    DRONE,
    DVD
}

/**
 * 패키지 제휴 업체 정보
 */
data class PartnerShop(
    val name: String,
    val link: String
) {
    init {
        require(name.isNotBlank()) { "제휴 업체 이름은 비어 있을 수 없습니다." }
        require(link.isNotBlank()) { "제휴 업체 링크는 비어 있을 수 없습니다." }
    }
}

/**
 * 상품 옵션 도메인 모델
 *
 * 요구사항 요약:
 * 1. 옵션명 (필수)
 * 2. 옵션 타입 (단품 / 패키지)
 * 3. 단품이면 의상 수, 촬영 장소, 촬영 시간(분) 모두 필수
 * 4. 패키지면 packageCategory, partnerShops 최소 1개 이상 필수
 * 5. 추가 가격(additionalPrice)은 0 이상
 */
data class ProductOption(
    val optionId: Long = 0L,
    val productId: Long,         // 상위 Product 식별자

    // 옵션명(필수)
    val name: String,

    // 옵션 타입 (단품 / 패키지)
    val optionType: OptionType,

    // 옵션 추가금(0 이상)
    val additionalPrice: Long,

    // 옵션 설명(선택)
    val description: String = "",

    // -----------------------
    // [단품] 필드들
    // -----------------------
    // 의상 수(필수)
    val costumeCount: Int = 0,

    // 촬영 장소(필수)
    val shootingLocation: String = "",

    // 촬영 시간(분)(필수)
    val shootingMinutes: Int = 0,

    // -----------------------
    // [패키지] 필드들
    // -----------------------
    // 패키지 종류(필수)
    val packageCategory: PackageCategory? = null,

    // 패키지 제휴 업체(최소 1개 이상 필요)
    val partnerShops: List<PartnerShop> = emptyList(),

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        // 옵션명 필수
        require(name.isNotBlank()) { "옵션명은 비어 있을 수 없습니다." }

        // 추가 가격은 0 이상
        require(additionalPrice >= 0) { "추가 가격은 음수가 될 수 없습니다." }

        if (optionType == OptionType.SINGLE) {
            // 단품 옵션에 필요한 필수값들 체크
            require(costumeCount > 0) { "단품 옵션은 의상 수가 1개 이상이어야 합니다." }
            require(shootingLocation.isNotBlank()) { "단품 옵션은 촬영 장소를 반드시 입력해야 합니다." }
            require(shootingMinutes > 0) { "단품 옵션은 촬영 시간이 1분 이상이어야 합니다." }
        } else {
            // 패키지 옵션에 필요한 필수값들 체크
            require(packageCategory != null) { "패키지 옵션에는 packageCategory가 필수입니다." }
            require(partnerShops.isNotEmpty()) { "패키지 옵션은 최소 1개 이상의 제휴 업체가 필요합니다." }
        }
    }
}
