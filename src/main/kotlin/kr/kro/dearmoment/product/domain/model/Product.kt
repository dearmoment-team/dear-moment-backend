package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

/**
 * 콘셉트(우아한, 빈티지, 모던, 클래식, 럭셔리 등)를 관리하기 위한 enum
 */
enum class ConceptType {
    ELEGANT, // 우아한
    VINTAGE, // 빈티지
    MODERN, // 모던
    CLASSIC, // 클래식
    LUXURY, // 고급스러운
}

/**
 * 연도별 상/하반기를 표현하기 위한 enum
 */
enum class SeasonHalf {
    FIRST_HALF, // 상반기
    SECOND_HALF, // 하반기
}

/**
 * 상품 도메인 모델
 */
data class Product(
    val productId: Long = 0L,
    val userId: Long,
    val title: String,
    val description: String = "",
    val price: Long,
    /**
     * 0=일반, 1=패키지 등
     */
    val typeCode: Int,
    /**
     * 우아한, 빈티지 등 여러 콘셉트
     */
    val concept: ConceptType,
    /**
     * 원본 제공 여부
     */
    val provideOriginal: Boolean = false,
    val shootingTime: LocalDateTime? = null,
    val shootingLocation: String = "",
    /**
     * 최대 의상 벌 수
     */
    val numberOfCostumes: Int = 0,
    /**
     * 25년 등 연도
     */
    val seasonYear: Int? = null,
    /**
     * 상반기/하반기 여부
     */
    val seasonHalf: SeasonHalf? = null,
    val partnerShops: List<PartnerShop> = emptyList(),
    val detailedInfo: String = "",
    val warrantyInfo: String = "",
    val contactInfo: String = "",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val options: List<ProductOption> = emptyList(),
    val images: List<String>,
) {
    init {
        require(images.isNotEmpty()) { "최소 1개 이상의 이미지가 필요합니다" }
        require(title.isNotBlank()) { "상품명은 필수 입력값입니다" }
        require(price >= 0) { "가격은 0 이상이어야 합니다" }

        // 패키지 상품이면 협력업체 필수
        if (typeCode == 1) {
            require(partnerShops.isNotEmpty()) {
                "패키지 상품은 하나 이상의 협력업체 정보가 필요합니다."
            }
            partnerShops.forEach {
                require(it.name.isNotBlank()) { "파트너샵 이름은 비어 있을 수 없습니다." }
                require(it.link.isNotBlank()) { "파트너샵 링크는 비어 있을 수 없습니다." }
            }
        }
    }

    val hasPackage: Boolean
        get() = (typeCode == 1)

    fun validateForUpdate() {
        require(productId != 0L) { "수정 시 상품 ID는 필수입니다" }
    }

    fun updateOptions(newOptions: List<ProductOption>): ProductOptionUpdateResult {
        val existingOptionsMap = options.associateBy { it.optionId }
        val newOptionsMap = newOptions.associateBy { it.optionId }

        val toDelete: Set<Long> =
            (existingOptionsMap.keys - newOptionsMap.keys)
                .filterNotNull()
                .toSet()

        val toUpdate =
            newOptions.filter {
                it.optionId != 0L && existingOptionsMap.containsKey(it.optionId)
            }.map { it.copy(productId = productId) }

        val toInsert =
            newOptions.filter {
                it.optionId == 0L
            }.map { it.copy(productId = productId) }

        val updatedOptions = toUpdate + toInsert
        return ProductOptionUpdateResult(updatedOptions, toDelete)
    }
}
