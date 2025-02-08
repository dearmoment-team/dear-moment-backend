package kr.kro.dearmoment.image.adapter.output.persistence

import kr.kro.dearmoment.image.application.port.output.DeleteImageFromDBPort
import kr.kro.dearmoment.image.application.port.output.GetImagePort
import kr.kro.dearmoment.image.application.port.output.SaveImagePort
import kr.kro.dearmoment.image.domain.Image
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ImagePersistenceAdapter(
    private val imageRepository: JpaImageRepository,
) : SaveImagePort, DeleteImageFromDBPort, GetImagePort {
    override fun save(image: Image): Long {
        val entity = ImageEntity.from(image)
        return imageRepository.save(entity).id
    }

    override fun saveAll(images: List<Image>): List<Long> {
        val entities = images.map { ImageEntity.from(it) }
        return imageRepository.saveAll(entities)
            .map { it.id }
    }

    override fun findAll(userId: Long): List<Image> {
        val entities = imageRepository.findAllByUserId(userId)
        return entities.map { ImageEntity.toDomain(it) }
    }

    override fun findOne(imageId: Long): Image {
        val entity =
            imageRepository.findByIdOrNull(imageId) ?: throw IllegalArgumentException("Invalid imageId: $imageId")
        return ImageEntity.toDomain(entity)
    }

    override fun delete(imageId: Long) {
        imageRepository.deleteById(imageId)
    }
}
