package kr.kro.dearmoment.product.application.service

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity

interface ProductImageService {
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
