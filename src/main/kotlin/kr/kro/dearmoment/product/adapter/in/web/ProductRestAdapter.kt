package kr.kro.dearmoment.product.adapter.`in`.web

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductRestAdapter(
    private val productUseCase: ProductUseCase
) {

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<*> {
        val createdProduct = productUseCase.saveProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: UpdateProductRequest
    ): ResponseEntity<*> {
        val updatedProduct = productUseCase.updateProduct(request.copy(productId = id))
        return ResponseEntity.ok(updatedProduct)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<*> {
        productUseCase.deleteProduct(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build<Void>()
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ResponseEntity<*> {
        val product = productUseCase.getProductById(id)
        return ResponseEntity.ok(product)
    }

    // 메인페이지 전용 엔드포인트 (추천순 → 최근일자 정렬)
    @GetMapping("/main")
    fun getMainPageProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<*> {
        val pagedResponse = productUseCase.getMainPageProducts(page, size)
        return ResponseEntity.ok(pagedResponse)
    }
}