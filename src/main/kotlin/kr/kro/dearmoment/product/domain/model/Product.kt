package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.image.domain.Image
import java.time.LocalDateTime

/**
 * 촬영 가능 시기를 표현하는 열거형
 * - 2025년 상반기, 하반기
 * - 2026년 상반기, 하반기
 */
enum class ShootingSeason {
    YEAR_2025_FIRST_HALF,
    YEAR_2025_SECOND_HALF,
    YEAR_2026_FIRST_HALF,
    YEAR_2026_SECOND_HALF
}

/**
 * 카메라 종류
 * - 디지털
 * - 필름
 */
enum class CameraType {
    DIGITAL, FILM
}

/**
 * 보정 스타일
 * - 요구사항에 제시된 (모던한, 시크한, 차분한, 빈티지한, 동화같은, 따뜻한, 몽환적인, 화사한, 내추럴한)에 맞춰 확장
 */
enum class RetouchStyle {
    MODERN,      // 모던한
    CHIC,        // 시크한
    CALM,        // 차분한
    VINTAGE,     // 빈티지한
    FAIRYTALE,   // 동화같은
    WARM,        // 따뜻한
    DREAMY,      // 몽환적인
    BRIGHT,      // 화사한
    NATURAL      // 내추럴한
}

/**
 * Product(상품) 도메인 모델
 *
 * 요구사항 요약:
 * 1. 상품명(필수)
 * 2. 촬영 가능 시기(다중 선택)
 * 3. 카메라 종류(다중 선택)
 * 4. 보정 스타일(최대 2개까지가 아니라, 요구사항상엔 '최대 2개' 문구가 있지만 실제 코드 구현에선 세부 제약 주석 참고)
 * 5. 대표 이미지 1장 (필수), 서브 이미지 4장 (필수)
 * 6. 추가 이미지 최대 5장
 * 7. 기본 가격 0원 이상
 * 8. 상품 상세 정보(detailedInfo), 연락처 정보(contactInfo) 등
 * 9. 옵션 목록 (단품/패키지)
 */
data class Product(
    val productId: Long = 0L,

    val userId: Long,

    // 상품명(필수)
    val title: String,

    // 간단 설명
    val description: String = "",

    // 기본 가격 (옵션 추가금 전까지의 금액, 0 이상)
    val basePrice: Long = 0,

    // 촬영 가능 시기(다중 선택)
    val availableSeasons: Set<ShootingSeason> = emptySet(),

    // 카메라 종류(다중 선택)
    val cameraTypes: Set<CameraType> = emptySet(),

    // 보정 스타일(다중 선택) - 실제 요구사항에서 "최대 2개까지"라면 추가 검증 로직 필요
    val retouchStyles: Set<RetouchStyle> = emptySet(),

    // 대표 이미지 (필수, null 불가)
    val mainImage: Image,

    // 서브(추가) 이미지. 요구사항상 4장 등록 필수라고 명시
    val subImages: List<Image> = emptyList(),

    // 추가 이미지. 최대 5장
    val additionalImages: List<Image> = emptyList(),

    // 기타 상세 정보
    val detailedInfo: String = "",

    // 연락처 정보
    val contactInfo: String = "",

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 여러 옵션(단품/패키지)
    val options: List<ProductOption> = emptyList()
) {
    init {
        // 상품명 필수 체크
        require(title.isNotBlank()) { "상품명은 필수 입력값입니다." }

        // 기본 가격 검증 (0 이상)
        require(basePrice >= 0) { "기본 가격은 0 이상이어야 합니다." }

        // 대표 이미지 검증
        requireNotNull(mainImage) { "대표 이미지는 필수입니다." }

        // 서브 이미지 4장 필수
        require(subImages.size == 4) {
            "서브 이미지는 정확히 4장 등록해야 합니다."
        }

        // 추가 이미지 최대 5장
        require(additionalImages.size <= 5) {
            "추가 이미지는 최대 5장까지 등록 가능합니다."
        }

        // (선택) 보정 스타일 최대 2개만 허용하고 싶다면 아래 로직 사용
        // require(retouchStyles.size <= 2) {
        //     "보정 스타일은 최대 2개까지만 선택할 수 있습니다."
        // }
    }

    /**
     * 상품 수정 시 호출하는 유효성 검증 메소드 예시
     */
    fun validateForUpdate() {
        require(productId != 0L) { "수정 시 상품 ID는 필수입니다." }
    }

    /**
     * 옵션 목록을 새로 업데이트할 때 사용되는 메소드
     * - 이미 존재하는 옵션은 갱신
     * - 새 옵션(식별자가 0L)은 삽입
     * - 기존 옵션 중 새 목록에 없는 것은 삭제 대상으로 분류
     */
    fun updateOptions(newOptions: List<ProductOption>): ProductOptionUpdateResult {
        val existingOptionsMap = options.associateBy { it.optionId }
        val newOptionsMap = newOptions.associateBy { it.optionId }

        // 삭제될 옵션: 기존에는 존재하지만 새 목록에는 없는 optionId
        val toDelete = (existingOptionsMap.keys - newOptionsMap.keys)
            .filterNotNull()
            .toSet()

        // 업데이트될 옵션: optionId != 0L && 기존에 존재하는 경우
        val toUpdate = newOptions
            .filter { it.optionId != 0L && existingOptionsMap.containsKey(it.optionId) }
            .map { it.copy(productId = productId) }

        // 새로 삽입될 옵션: optionId == 0L
        val toInsert = newOptions
            .filter { it.optionId == 0L }
            .map { it.copy(productId = productId) }

        val updatedOptions = toUpdate + toInsert

        return ProductOptionUpdateResult(
            updatedOptions = updatedOptions,
            deletedOptionIds = toDelete
        )
    }
}

