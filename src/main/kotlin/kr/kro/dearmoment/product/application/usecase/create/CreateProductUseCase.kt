package kr.kro.dearmoment.product.application.usecase.create

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.springframework.web.multipart.MultipartFile

interface CreateProductUseCase {
    fun saveProduct(
        request: CreateProductRequest,
        mainImageFile: MultipartFile,
        subImageFiles: List<MultipartFile>,
        additionalImageFiles: List<MultipartFile>,
    ): ProductResponse
}
