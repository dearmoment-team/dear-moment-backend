package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.image.domain.Image
import java.time.LocalDateTime

/**
 * 촬영 가능 시기
 */
enum class ShootingSeason {
    YEAR_2025_FIRST_HALF,
    YEAR_2025_SECOND_HALF,
    YEAR_2026_FIRST_HALF,
    YEAR_2026_SECOND_HALF,
}

/**
 * 카메라 종류
 */
enum class CameraType {
    DIGITAL,
    FILM,
}

/**
 * 보정 스타일
 */
enum class RetouchStyle {
    MODERN,
    CHIC,
    CALM,
    VINTAGE,
    FAIRYTALE,
    WARM,
    DREAMY,
    BRIGHT,
    NATURAL,
}

/**
 * 상품 유형 (MVP 예: WEDDING_SNAP)
 */
enum class ProductType {
    WEDDING_SNAP,
}

/**
 * 촬영 장소 (MVP 예: JEJU)
 */
enum class ShootingPlace {
    JEJU,
}

/**
 * Product(상품) 도메인 모델
 */
data class Product(
    val productId: Long = 0L,
    val userId: Long,

    val productType: ProductType,
    val shootingPlace: ShootingPlace,

    // 상품명
    val title: String,
    // 간단 설명
    val description: String = "",

    // 촬영 가능 시기(다중 선택)
    val availableSeasons: Set<ShootingSeason> = emptySet(),
    // 카메라 종류(다중 선택)
    val cameraTypes: Set<CameraType> = emptySet(),
    // 보정 스타일(다중 선택)
    val retouchStyles: Set<RetouchStyle> = emptySet(),

    // 대표 이미지 (필수)
    val mainImage: Image,
    // 서브 이미지(4장 필수)
    val subImages: List<Image> = emptyList(),
    // 추가 이미지(최대 5장)
    val additionalImages: List<Image> = emptyList(),

    // 상세 정보, 연락처
    val detailedInfo: String = "",
    val contactInfo: String = "",

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 여러 옵션
    val options: List<ProductOption> = emptyList(),
) {
    init {
        require(title.isNotBlank()) { "상품명은 필수 입력값입니다." }

        require(subImages.size == 4) {
            "서브 이미지는 정확히 4장 등록해야 합니다."
        }
        // 추가 이미지 최대 5장 검증
        require(additionalImages.size <= 5) {
            "추가 이미지는 최대 5장까지 등록 가능합니다."
        }
        // 보정 스타일은 최대 2개까지만 선택 가능하도록 제한
        require(retouchStyles.size <= 2) {
            "보정 스타일은 최대 2개까지만 선택할 수 있습니다."
        }
    }

    fun validateForUpdate() {
        require(productId != 0L) { "수정 시 상품 ID는 필수입니다." }
    }

    /**
     * 옵션 목록 업데이트 시 사용하는 예시 로직
     */
    fun updateOptions(newOptions: List<ProductOption>): ProductOptionUpdateResult {
        val existingOptionsMap = options.associateBy { it.optionId }
        val newOptionsMap = newOptions.associateBy { it.optionId }

        // 삭제될 옵션
        val toDelete =
            (existingOptionsMap.keys - newOptionsMap.keys).toList().toSet()

        // 업데이트될 옵션: id != 0 && 기존에 존재
        val toUpdate = newOptions
            .filter { it.optionId != 0L && existingOptionsMap.containsKey(it.optionId) }
            .map { it.copy(productId = productId) }

        // 새로 삽입될 옵션: id == 0
        val toInsert = newOptions
            .filter { it.optionId == 0L }
            .map { it.copy(productId = productId) }

        return ProductOptionUpdateResult(
            updatedOptions = toUpdate + toInsert,
            deletedOptionIds = toDelete
        )
    }
}