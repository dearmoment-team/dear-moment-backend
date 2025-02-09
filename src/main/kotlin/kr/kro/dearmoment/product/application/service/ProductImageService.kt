package kr.kro.dearmoment.product.application.service

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import org.springframework.web.multipart.MultipartFile

interface ProductImageService {
    fun uploadImages(
        images: List<MultipartFile>,
        userId: Long,
    ): List<Image>

    fun uploadNewImagesWithPlaceholders(
        newImages: List<MultipartFile>?,
        userId: Long,
    ): Map<String, Image>

    fun resolveFinalImageOrder(
        requestedImageIdentifiers: List<String>,
        newImageMappings: Map<String, Image>,
        userId: Long,
    ): List<Image>

    fun synchronizeProductImages(
        existingProduct: ProductEntity,
        finalImages: List<Image>,
        newImageMappings: Map<String, Image>,
        userId: Long,
    )
}
