package kr.kro.dearmoment.product.adapter.input.web

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
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductRestAdapter(
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val productSearchUseCase: ProductSearchUseCase,
) {
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createProduct(
        @ModelAttribute request: CreateProductRequest,
    ): ProductResponse {
        return createProductUseCase.saveProduct(request)
    }

    @PutMapping(
        value = ["/{id}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateProduct(
        @PathVariable id: Long,
        @ModelAttribute request: UpdateProductRequest,
    ): ProductResponse {
        return updateProductUseCase.updateProduct(request.copy(productId = id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @PathVariable id: Long,
    ) {
        deleteProductUseCase.deleteProduct(id)
    }

    @GetMapping(
        value = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getProduct(
        @PathVariable id: Long,
    ): ProductResponse {
        return getProductUseCase.getProductById(id)
    }

    @GetMapping(
        value = ["/main"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getMainPageProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PagedResponse<ProductResponse> {
        return productSearchUseCase.getMainPageProducts(page, size)
    }

    @GetMapping(
        value = ["/search"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun searchProducts(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) productType: String?,
        @RequestParam(required = false) shootingPlace: String?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PagedResponse<ProductResponse> {
        return productSearchUseCase.searchProducts(title, productType, shootingPlace, sortBy, page, size)
    }
}
