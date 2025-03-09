package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 특정 조건에 따라 Product를 검색하기 위한 DTO.
 *
 * 기존에는 basePrice를 기준으로 검색하였으나, 도메인 변경에 따라
 * 이제는 상품의 제목, 상품 유형(productType), 촬영 장소(shootingPlace) 등을
 * 조건으로 검색합니다.
 */
@Schema(description = "상품 검색 요청 DTO")
data class ProductSearchRequest(
    @Schema(description = "검색할 Product의 제목", example = "웨딩")
    val title: String? = null,
    @Schema(
        description = "검색할 상품 유형 (도메인: ProductType)",
        example = "WEDDING_SNAP",
        allowableValues = ["WEDDING_SNAP"]
    )
    val productType: String? = null,
    @Schema(
        description = "검색할 촬영 장소 (도메인: ShootingPlace)",
        example = "JEJU",
        allowableValues = ["JEJU"]
    )
    val shootingPlace: String? = null,
    @Schema(
        description = "정렬 조건",
        example = "created-desc",
        allowableValues = ["created-desc", "created-asc"]
    )
    val sortBy: String? = null,
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    val page: Int = 0,
    @Schema(description = "한 페이지에 보여줄 상품 수", example = "10")
    val size: Int = 10,
)
