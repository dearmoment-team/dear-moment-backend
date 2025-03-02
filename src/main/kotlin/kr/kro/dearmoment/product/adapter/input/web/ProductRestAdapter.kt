package kr.kro.dearmoment.product.adapter.input.web

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.springframework.http.HttpStatus
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
    @PostMapping(consumes = ["multipart/form-data"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @ModelAttribute request: CreateProductRequest,
    ): ProductResponse {
        return createProductUseCase.saveProduct(request)
    }

    @PutMapping("/{id}", consumes = ["multipart/form-data"])
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

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: Long,
    ): ProductResponse {
        return getProductUseCase.getProductById(id)
    }

    // 메인페이지 전용 엔드포인트 (추천순 → 최근일자 정렬)
    @GetMapping("/main")
    fun getMainPageProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PagedResponse<ProductResponse> {
        return productSearchUseCase.getMainPageProducts(page, size)
    }
}
