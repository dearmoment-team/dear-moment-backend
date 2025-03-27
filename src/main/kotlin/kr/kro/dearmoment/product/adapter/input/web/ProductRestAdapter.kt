package kr.kro.dearmoment.product.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
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
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RequestParam
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
        @Parameter(description = "상품 옵션 목록 (JSON)", required = false)
        @RequestPart(value = "options", required = false)
        options: List<UpdateProductOptionRequest>?,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): ProductResponse {
        val updateRequest = rawRequest ?: UpdateProductRequest(
            productId = id,
            studioId = 0L
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
        @AuthenticationPrincipal(expression = "id") userId: UUID
    ) {
        // 인증된 userId와 함께 삭제 UseCase 호출 (내부에서 소유권 검증 수행)
        deleteProductUseCase.deleteProduct(userId, id)
    }

    // 4. 상품 상세 조회
    @Operation(summary = "상품 상세 조회", description = "특정 상품을 상세 조회합니다.")
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
        value = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getProduct(
        @Parameter(description = "조회할 상품의 식별자", required = true)
        @PathVariable id: Long,
    ): ProductResponse {
        return getProductUseCase.getProductById(id)
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
    @GetMapping(
        value = ["/main"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getMainPageProducts(
        @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int,
    ): PagedResponse<ProductResponse> {
        return productSearchUseCase.getMainPageProducts(page, size)
    }

    // 6. 상품 검색
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
    @GetMapping(
        value = ["/search"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun searchProducts(
        @Parameter(description = "검색할 상품 제목") @RequestParam(required = false) title: String?,
        @Parameter(description = "검색할 상품 타입") @RequestParam(required = false) productType: String?,
        @Parameter(description = "촬영 장소") @RequestParam(required = false) shootingPlace: String?,
        @Parameter(description = "정렬 기준") @RequestParam(required = false) sortBy: String?,
        @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int,
    ): PagedResponse<ProductResponse> {
        return productSearchUseCase.searchProducts(title, productType, shootingPlace, sortBy, page, size)
    }

    // 7. 상품 옵션 삭제
    @Operation(summary = "상품 옵션 삭제", description = "특정 상품에 속한 옵션을 삭제합니다.")
    @ApiResponse(
        responseCode = "204",
        description = "옵션 삭제 성공",
        content = [Content(schema = Schema())],
    )
    @DeleteMapping("/{productId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOption(
        @Parameter(description = "상품 ID") @PathVariable productId: Long,
        @Parameter(description = "옵션 ID") @PathVariable optionId: Long,
    ) {
        deleteProductOptionUseCase.deleteOption(productId, optionId)
    }
}
