package kr.kro.dearmoment.product.adapter.input.web

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
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

@RestController
@RequestMapping("/api/products")
class ProductRestAdapter(
    private val productUseCase: ProductUseCase
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @RequestPart("request") request: CreateProductRequest,
        @RequestPart("images") images: List<MultipartFile>
    ): ProductResponse {
        return productUseCase.saveProduct(request, images)
    }

    @PutMapping(value = ["/{id}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProduct(
        @PathVariable id: Long,
        @RequestPart("request") request: UpdateProductRequest,
        @RequestPart("images", required = false) images: List<MultipartFile>?
    ): ProductResponse {
        return productUseCase.updateProduct(request, images)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @PathVariable id: Long
    ) {
        productUseCase.deleteProduct(id)
    }

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: Long
    ): ProductResponse {
        return productUseCase.getProductById(id)
    }

    @GetMapping("/main")
    fun getMainPageProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): PagedResponse<ProductResponse> {
        return productUseCase.getMainPageProducts(page, size)
    }
}
