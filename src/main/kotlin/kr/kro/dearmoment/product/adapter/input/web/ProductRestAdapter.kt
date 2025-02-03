package kr.kro.dearmoment.product.adapter.input.web

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductRestAdapter(
    private val productUseCase: ProductUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @RequestBody request: CreateProductRequest,
    ): ProductResponse {
        return productUseCase.saveProduct(request)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: UpdateProductRequest,
    ): ProductResponse {
        return productUseCase.updateProduct(request.copy(productId = id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @PathVariable id: Long,
    ) {
        productUseCase.deleteProduct(id)
    }

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: Long,
    ): ProductResponse {
        return productUseCase.getProductById(id)
    }

    // 메인페이지 전용 엔드포인트 (추천순 → 최근일자 정렬)
    @GetMapping("/main")
    fun getMainPageProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PagedResponse<ProductResponse> {
        return productUseCase.getMainPageProducts(page, size)
    }
}
