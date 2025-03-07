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

@Service
class CreateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val imageService: ImageService,
    private val productOptionUseCase: ProductOptionUseCase,
) : CreateProductUseCase {
    @Transactional
    override fun saveProduct(request: CreateProductRequest): ProductResponse {
        // 서브 이미지 / 추가 이미지 개수 검증
        if (request.subImageFiles.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${request.subImageFiles.size}장입니다.")
        }
        if (request.additionalImageFiles.size > 5) {
            throw IllegalArgumentException("추가 이미지는 최대 5장까지만 가능합니다. 현재 ${request.additionalImageFiles.size}장입니다.")
        }

        // 메인 이미지 업로드 (메인 이미지는 필수)
        val mainImageFile = request.mainImageFile ?: throw IllegalArgumentException("메인 이미지는 필수입니다.")
        val mainImg =
            imageService.save(
                SaveImageCommand(
                    file = mainImageFile,
                    userId = request.userId,
                ),
            )

        // 서브 이미지 업로드
        val subImgs =
            request.subImageFiles.map {
                imageService.save(
                    SaveImageCommand(
                        file = it,
                        userId = request.userId,
                    ),
                )
            }

        // 추가 이미지 업로드
        val additionalImgs =
            request.additionalImageFiles.map {
                imageService.save(
                    SaveImageCommand(
                        file = it,
                        userId = request.userId,
                    ),
                )
            }

        // 도메인 객체 생성
        val product =
            CreateProductRequest.toDomain(
                req = request,
                mainImageUrl = mainImg.url,
                subImagesUrls = subImgs.map { it.url },
                additionalImagesUrls = additionalImgs.map { it.url },
            )

        // 동일 제목 중복 체크
        validateForCreation(product)

        // 상품 저장
        val savedProduct = productPersistencePort.save(product)

        try {
            request.options.forEach { optionDto ->
                productOptionUseCase.saveProductOption(savedProduct.productId, optionDto)
            }
        } catch (e: Exception) {
            throw IllegalStateException("상품 옵션 저장 중 문제가 발생했습니다. 옵션 저장에 실패하였습니다.", e)
        }

        return ProductResponse.fromDomain(savedProduct)
    }

    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "동일 제목의 상품이 이미 존재합니다: ${product.title}"
        }
    }
}
