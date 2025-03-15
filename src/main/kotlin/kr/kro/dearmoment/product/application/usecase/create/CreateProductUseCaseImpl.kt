package kr.kro.dearmoment.product.application.usecase.create

import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class CreateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val imageService: ImageService,
    private val productOptionUseCase: ProductOptionUseCase,
) : CreateProductUseCase {
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        mainImageFile: MultipartFile,
        subImageFiles: List<MultipartFile>,
        additionalImageFiles: List<MultipartFile>,
    ): ProductResponse {
        // 서브 이미지 검증
        if (subImageFiles.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${subImageFiles.size}장입니다.")
        }
        // 추가 이미지 검증
        if (additionalImageFiles.size > 5) {
            throw IllegalArgumentException("추가 이미지는 최대 5장까지만 가능합니다. 현재 ${additionalImageFiles.size}장입니다.")
        }

        // 이미지 업로드
        val mainImg = imageService.save(SaveImageCommand(mainImageFile, request.userId))
        val subImgs = subImageFiles.map { imageService.save(SaveImageCommand(it, request.userId)) }
        val additionalImgs = additionalImageFiles.map { imageService.save(SaveImageCommand(it, request.userId)) }

        // 도메인 객체 생성
        val product =
            CreateProductRequest.toDomain(
                req = request,
                mainImageUrl = mainImg.url,
                subImagesUrls = subImgs.map { it.url },
                additionalImagesUrls = additionalImgs.map { it.url },
            )

        validateForCreation(product)
        val savedProduct = productPersistencePort.save(product)

        // 옵션 저장
        request.options.forEach {
            productOptionUseCase.saveProductOption(savedProduct.productId, it)
        }

        return ProductResponse.fromDomain(savedProduct)
    }

    private fun validateForCreation(product: Product) {
        if (productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            throw IllegalArgumentException("동일 제목의 상품이 이미 존재합니다: ${product.title}")
        }
    }
}
