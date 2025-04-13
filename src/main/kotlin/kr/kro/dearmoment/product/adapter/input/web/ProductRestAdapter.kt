package kr.kro.dearmoment.product.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.domain.SortCriteria
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.GetProductResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductOptionUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Tag(name = "Product API", description = "상품과 관련된 API")
@RestController
@RequestMapping("/api/products")
class ProductRestAdapter(
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val productSearchUseCase: ProductSearchUseCase,
    private val deleteProductOptionUseCase: DeleteProductOptionUseCase,
) {
    // 1. 상품 생성
    @Operation(summary = "상품 생성")
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createProduct(
        @AuthenticationPrincipal(expression = "id") userId: UUID,
        @Parameter(
            description = "상품 정보(JSON)",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CreateProductRequest::class),
                ),
            ],
        )
        @Valid
        @RequestPart("request") request: CreateProductRequest,
        @Parameter(
            description = "대표 이미지 파일",
            required = true,
            content = [
                Content(
                    mediaType = "image/*",
                    schema = Schema(type = "string", format = "binary"),
                ),
            ],
        )
        @RequestPart("mainImageFile") mainImageFile: MultipartFile,
        @Parameter(
            description = "서브 이미지 파일 (4장)",
            required = true,
            content = [
                Content(
                    mediaType = "image/*",
                    array = ArraySchema(schema = Schema(type = "string", format = "binary")),
                ),
            ],
        )
        @RequestPart("subImageFiles") subImageFiles: List<MultipartFile>,
        @Parameter(
            description = "추가 이미지 파일 (선택적, 최대 5장)",
            required = false,
            content = [
                Content(
                    mediaType = "image/*",
                    array = ArraySchema(schema = Schema(type = "string", format = "binary")),
                ),
            ],
        )
        @RequestPart(value = "additionalImageFiles", required = false)
        additionalImageFiles: List<MultipartFile> = emptyList(),
    ): ProductResponse {
        return createProductUseCase.saveProduct(
            request = request,
            userId = userId,
            mainImageFile = mainImageFile,
            subImageFiles = subImageFiles,
            additionalImageFiles = additionalImageFiles,
        )
    }

    // 2. 상품 부분 수정
    @Operation(
        summary = "상품 부분 수정",
        description = "상품 정보 중 일부만 수정합니다. 사용자 ID는 인증 principal에서 가져오기 때문에 DTO에는 포함되지 않습니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "상품 수정 성공",
        content = [Content(schema = Schema(implementation = ProductResponse::class))],
    )
    @PatchMapping(
        value = ["/{id}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateProduct(
        @Parameter(description = "상품 식별자", required = true)
        @PathVariable("id") id: Long,
        @Parameter(
            description = "상품 수정 요청 정보 (기본정보 및 메타데이터, 사용자 ID는 인증 principal에서 처리됨)",
            required = false,
        )
        @Valid
        @RequestPart(value = "request", required = false)
        rawRequest: UpdateProductRequest?,
        @Parameter(description = "대표 이미지 파일", required = false)
        @RequestPart(value = "mainImageFile", required = false)
        mainImageFile: MultipartFile?,
        @Parameter(description = "서브 이미지 파일 목록", required = false)
        @RequestPart(value = "subImageFiles", required = false)
        subImageFiles: List<MultipartFile>?,
        @Parameter(description = "추가 이미지 파일 목록", required = false)
        @RequestPart(value = "additionalImageFiles", required = false)
        additionalImageFiles: List<MultipartFile>?,
        @Valid @Parameter(description = "상품 옵션 목록 (JSON)", required = false)
        @RequestPart(value = "options", required = false)
        options: List<UpdateProductOptionRequest>?,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): ProductResponse {
        val updateRequest =
            rawRequest ?: UpdateProductRequest(
                productId = id,
                studioId = 0L,
            )

        return updateProductUseCase.updateProduct(
            userId = userId,
            productId = id,
            rawRequest = updateRequest,
            mainImageFile = mainImageFile,
            subImageFiles = subImageFiles,
            additionalImageFiles = additionalImageFiles,
            options = options ?: emptyList(),
        )
    }

    // 3. 상품 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @Parameter(description = "삭제할 상품의 식별자", required = true)
        @PathVariable id: Long,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ) {
        deleteProductUseCase.deleteProduct(userId, id)
    }

    // 4. 상품 상세 조회
    @Operation(summary = "상품 상세 조회", description = "특정 상품을 상세 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 조회 성공",
                content = [Content(schema = Schema(implementation = GetProductResponse::class))],
            ),
        ],
    )
    @GetMapping(
        value = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getProduct(
        @Parameter(description = "조회할 상품의 식별자", required = true)
        @PathVariable id: Long,
        @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : id") userId: UUID?,
    ): GetProductResponse {
        return getProductUseCase.getProductById(id, userId)
    }

    // 5. 메인 페이지 상품 조회
    @Operation(summary = "메인 페이지 상품 조회", description = "메인 페이지에 노출할 상품 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "메인 페이지 상품 조회 성공",
                content = [Content(schema = Schema(implementation = PagedResponse::class))],
            ),
        ],
    )
    @GetMapping("/main")
    fun getMainPageProducts(
        @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : id") userId: UUID?,
    ): PagedResponse<SearchProductResponse> {
        return productSearchUseCase.searchProducts(userId, SearchProductRequest(), page, size)
    }

    @Operation(summary = "상품 검색", description = "상품을 조건에 맞게 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 검색 성공",
                content = [Content(schema = Schema(implementation = PagedResponse::class))],
            ),
        ],
    )
    @GetMapping("/search")
    fun searchProducts(
        @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int,
        @Schema(
            description = "정렬 기준 (기본 값: \"RECOMMENDED\")",
            allowableValues = ["RECOMMENDED", "POPULAR", "PRICE_LOW", "PRICE_HIGH"],
            example = "[\"PRICE_LOW\"]",
        )
        @RequestParam(required = false) sortBy: String = SortCriteria.POPULAR.name,
        @Schema(
            description = "촬영 가능 시기",
            allowableValues =
                ["YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF", "YEAR_2026_FIRST_HALF", "YEAR_2026_SECOND_HALF"],
            example = "[\"YEAR_2025_FIRST_HALF\",\"YEAR_2025_SECOND_HALF\"]",
        )
        @RequestParam(required = false) availableSeasons: List<String> = emptyList(),
        @Schema(
            description = "카메라 종류",
            allowableValues = ["DIGITAL", "FILM"],
            example = "[\"DIGITAL\"]",
        )
        @RequestParam(required = false) cameraTypes: List<String> = emptyList(),
        @Schema(
            description = "보정 스타일",
            allowableValues = [
                "MODERN", "CHIC", "CALM", "VINTAGE",
                "FAIRYTALE", "WARM", "DREAMY", "BRIGHT", "NATURAL",
            ],
            example = "[\"MODERN\", \"FAIRYTALE\"]",
        )
        @RequestParam(required = false) retouchStyles: List<String> = emptyList(),
        @Schema(
            description = "제휴 업체",
            allowableValues = ["HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
            example = "[\"HAIR_MAKEUP\"]",
        )
        @RequestParam(required = false) partnerShopCategories: List<String> = emptyList(),
        @RequestParam(required = false, defaultValue = "0") minPrice: Long = 0L,
        @RequestParam(required = false, defaultValue = "10000000") maxPrice: Long = 10_000_000L,
        @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : id") userId: UUID?,
    ): PagedResponse<SearchProductResponse> {
        val request =
            SearchProductRequest(
                sortBy = sortBy,
                availableSeasons = availableSeasons,
                cameraTypes = cameraTypes,
                retouchStyles = retouchStyles,
                partnerShopCategories = partnerShopCategories,
                minPrice = minPrice,
                maxPrice = maxPrice,
            )
        return productSearchUseCase.searchProducts(userId, request, page, size)
    }

    // 7. 상품 옵션 삭제
    @Operation(summary = "상품 옵션 삭제", description = "특정 상품에 속한 옵션을 삭제합니다. 사용자 ID는 인증 principal에서 가져오기 때문에 DTO에는 포함되지 않습니다.")
    @ApiResponse(
        responseCode = "204",
        description = "옵션 삭제 성공",
        content = [Content(schema = Schema())],
    )
    @DeleteMapping("/{productId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOption(
        @Parameter(description = "상품 ID", required = true) @PathVariable productId: Long,
        @Parameter(description = "옵션 ID", required = true) @PathVariable optionId: Long,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ) {
        deleteProductOptionUseCase.deleteOption(userId, productId, optionId)
    }

    @Operation(
        summary = "내 상품 상세 조회",
        description = "현재 인증된 사용자가 소유한 상품의 상세 정보를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 조회 성공",
                content = [Content(schema = Schema(implementation = ProductResponse::class))],
            ),
        ],
    )
    @GetMapping(
        value = ["/mine"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getMyProduct(
        @Parameter(description = "현재 인증된 사용자의 ID", required = true)
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): List<GetProductResponse> {
        return getProductUseCase.getMyProduct(userId)
    }
}
