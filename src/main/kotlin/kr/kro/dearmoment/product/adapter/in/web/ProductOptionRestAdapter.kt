package kr.kro.dearmoment.product.adapter.`in`.web

import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.usecase.ProductOptionUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products/{productId}/options")
class ProductOptionRestAdapter(
    private val productOptionUseCase: ProductOptionUseCase
) {

    @PostMapping
    fun createOption(
        @PathVariable productId: Long,
        @RequestBody request: CreateProductOptionRequest
    ): ResponseEntity<*> {
        val createdOption = productOptionUseCase.saveProductOption(productId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOption)
    }

    @GetMapping("/{optionId}")
    fun getOption(
        @PathVariable productId: Long,
        @PathVariable optionId: Long
    ): ResponseEntity<*> {
        val option = productOptionUseCase.getProductOptionById(optionId)
        return ResponseEntity.ok(option)
    }

    @DeleteMapping("/{optionId}")
    fun deleteOption(
        @PathVariable productId: Long,
        @PathVariable optionId: Long
    ): ResponseEntity<*> {
        productOptionUseCase.deleteProductOptionById(optionId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build<Void>()
    }
}