package kr.kro.dearmoment.product.application.service

import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import org.springframework.stereotype.Service

@Service
class ProductImageServiceImpl(
    private val imageService: ImageService,
) : ProductImageService {

    override fun resolveFinalImageOrder(
        requestedImageIdentifiers: List<String>,
        newImageMappings: Map<String, Image>,
        userId: Long,
    ): List<Image> {
        return requestedImageIdentifiers.map { identifier ->
            if (identifier.contains("://")) {
                val fileName = extractFileNameFromUrl(identifier)
                Image(userId = userId, fileName = fileName, url = identifier)
            } else {
                newImageMappings[identifier] ?: Image(userId = userId, fileName = identifier, url = "")
            }
        }
    }

    override fun synchronizeProductImages(
        existingProduct: ProductEntity,
        finalImages: List<Image>,
        newImageMappings: Map<String, Image>,
        userId: Long,
    ) {
        val existingImages = existingProduct.images.associateBy { it.fileName }
        val desiredFileNames = finalImages.map { it.fileName }.toSet()
        existingProduct.images.filterNot { desiredFileNames.contains(it.fileName) }
            .forEach { imageService.delete(it.id) }
        val updatedImages =
            finalImages.map { image ->
                existingImages[image.fileName] ?: createNewImageEntity(image, existingProduct, newImageMappings)
            }
        existingProduct.images.clear()
        existingProduct.images.addAll(updatedImages)
    }

    private fun createNewImageEntity(
        image: Image,
        product: ProductEntity,
        newImageMappings: Map<String, Image>,
    ): ImageEntity {
        if (newImageMappings.values.any { it.fileName == image.fileName }) {
            return ImageEntity.from(image).apply { this.product = product }
        }
        throw IllegalArgumentException("Invalid image reference: ${image.fileName}")
    }

    private fun extractFileNameFromUrl(url: String): String {
        return url.substringAfterLast('/')
    }
}
