package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface UpdateProductUseCase {
    fun updateProduct(
        userId: UUID,
        productId: Long,
        rawRequest: UpdateProductRequest,
        mainImageFile: MultipartFile?,
        subImageFiles: List<MultipartFile>?,
        additionalImageFiles: List<MultipartFile>?,
        options: List<UpdateProductOptionRequest>?
    ): ProductResponse
}
