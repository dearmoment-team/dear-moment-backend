package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.springframework.web.multipart.MultipartFile

interface UpdateProductUseCase {
    /**
     * 상품 수정
     * - [productId]: 수정할 상품의 ID
     * - [rawRequest]: JSON으로 받은 수정 요청 DTO
     * - [mainImageFile]: 새로 업로드할 대표 이미지 (없으면 null)
     * - [subImageFiles]: 새로 업로드할 서브 이미지 목록 (없으면 null)
     * - [additionalImageFiles]: 새로 업로드할 추가 이미지 목록 (없으면 null)
     */
    fun updateProduct(
        productId: Long,
        rawRequest: UpdateProductRequest,
        mainImageFile: MultipartFile?,
        subImageFiles: List<MultipartFile>?,
        additionalImageFiles: List<MultipartFile>?,
    ): ProductResponse
}
