package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import kr.kro.dearmoment.product.domain.model.option.ProductOptionUpdateResult
import kr.kro.dearmoment.studio.domain.Studio
import java.time.LocalDateTime
import java.util.UUID

/**
 * 상품 도메인 모델
 */
data class Product(
    val productId: Long = 0L,
    val userId: UUID = UUID.randomUUID(),
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
    val studio: Studio? = null,
    val likeCount: Long = 0L,
    val optionLikeCount: Long = 0L,
    val inquiryCount: Long = 0L,
) {
    init {
        require(title.isNotBlank()) { "상품명은 필수 입력값입니다." }
        require(subImages.size == 4) {
            "서브 이미지는 정확히 4장 등록해야 합니다."
        }
        require(additionalImages.size <= 5) {
            "추가 이미지는 최대 5장까지 등록 가능합니다."
        }
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
        val toDelete = (existingOptionsMap.keys - newOptionsMap.keys).toList().toSet()

        // 업데이트될 옵션: id != 0 && 기존에 존재
        val toUpdate =
            newOptions
                .filter { it.optionId != 0L && existingOptionsMap.containsKey(it.optionId) }
                .map { it.copy(productId = productId) }

        // 새로 삽입될 옵션: id == 0
        val toInsert =
            newOptions
                .filter { it.optionId == 0L }
                .map { it.copy(productId = productId) }

        return ProductOptionUpdateResult(
            updatedOptions = toUpdate + toInsert,
            deletedOptionIds = toDelete,
        )
    }
}
