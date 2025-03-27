package kr.kro.dearmoment.product.application.usecase.create

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface CreateProductUseCase {
    fun saveProduct(
        request: CreateProductRequest,
        userId: UUID,
        mainImageFile: MultipartFile,
        subImageFiles: List<MultipartFile>,
        additionalImageFiles: List<MultipartFile>,
    ): ProductResponse
}
