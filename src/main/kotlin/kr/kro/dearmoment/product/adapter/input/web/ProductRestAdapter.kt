package kr.kro.dearmoment.product.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Product API", description = "상품과 관련된 API")
@RestController
@RequestMapping("/api/products")
class ProductRestAdapter(
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val productSearchUseCase: ProductSearchUseCase,
) {
    @Operation(
        summary = "상품 생성"
    )
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createProduct(
        @Parameter(
            description = "상품 정보(JSON)",
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = CreateProductRequest::class)
            )]
        )
        @RequestPart("request") request: CreateProductRequest,

        @Parameter(
            description = "대표 이미지 파일",
            required = true,
            content = [Content(
                mediaType = "image/*",
                schema = Schema(type = "string", format = "binary")
            )]
        )
        @RequestPart("mainImageFile") mainImageFile: MultipartFile,

        @Parameter(
            description = "서브 이미지 파일 (4장)",
            required = true,
            content = [Content(
                mediaType = "image/*",
                array = ArraySchema(
                    schema = Schema(type = "string", format = "binary")
                )
            )]
        )
        @RequestPart("subImageFiles") subImageFiles: List<MultipartFile>,

        @Parameter(
            description = "추가 이미지 파일 (선택적, 최대 5장)",
            required = false,
            content = [Content(
                mediaType = "image/*",
                array = ArraySchema(
                    schema = Schema(type = "string", format = "binary")
                )
            )]
        )
        @RequestPart(value = "additionalImageFiles", required = false)
        additionalImageFiles: List<MultipartFile> = emptyList()
    ): ProductResponse {
        return createProductUseCase.saveProduct(
            request = request,
            mainImageFile = mainImageFile,
            subImageFiles = subImageFiles,
            additionalImageFiles = additionalImageFiles
        )
    }


    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 수정 성공",
                content = [Content(schema = Schema(implementation = ProductResponse::class))],
            ),
        ],
    )
    @PutMapping(
        value = ["/{id}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateProduct(
        @Parameter(description = "수정할 상품의 식별자", required = true)
        @PathVariable id: Long,
        @Parameter(description = "수정할 상품 정보(JSON)", required = true)
        @RequestPart("request")
        rawRequest: UpdateProductRequest,
        @Parameter(description = "새 대표 이미지 파일 (없으면 null)", required = false)
        @RequestPart(value = "mainImageFile", required = false)
        mainImageFile: MultipartFile?,
        @Parameter(description = "새로 업로드할 서브 이미지 파일들(최대 4개)", required = false)
        @RequestPart(value = "subImageFiles", required = false)
        subImageFiles: List<MultipartFile>?,
        @Parameter(description = "새로 업로드할 추가 이미지 파일들(최대 5개)", required = false)
        @RequestPart(value = "additionalImageFiles", required = false)
        additionalImageFiles: List<MultipartFile>?,
    ): ProductResponse {
        // 컨트롤러는 받은 파라미터를 그대로 UseCase로 넘김
        return updateProductUseCase.updateProduct(
            productId = id,
            rawRequest = rawRequest,
            mainImageFile = mainImageFile,
            subImageFiles = subImageFiles,
            additionalImageFiles = additionalImageFiles,
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @Parameter(description = "삭제할 상품의 식별자", required = true)
        @PathVariable id: Long,
    ) {
        deleteProductUseCase.deleteProduct(id)
    }

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
}
