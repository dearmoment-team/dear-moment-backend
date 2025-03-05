package kr.kro.dearmoment.image.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.port.input.UpdateImagePort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromDBPort
import kr.kro.dearmoment.image.application.port.output.GetImagePort
import kr.kro.dearmoment.image.application.port.output.SaveImagePort
import kr.kro.dearmoment.image.domain.Image
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ImagePersistenceAdapter(
    private val imageRepository: JpaImageRepository,
) : SaveImagePort, UpdateImagePort, DeleteImageFromDBPort, GetImagePort {
    override fun save(image: Image): Image {
        val entity = ImageEntity.from(image)
        return imageRepository.save(entity).toDomain()
    }

    override fun saveAll(images: List<Image>): List<Long> {
        val entities = images.map { ImageEntity.from(it) }
        return imageRepository.saveAll(entities)
            .map { it.id }
    }

    override fun findUserImages(userId: Long): List<Image> =
        imageRepository.findAllByUserId(userId)
            .map { it.toDomain() }

    override fun findOne(imageId: Long): Image {
        val entity =
            imageRepository.findByIdOrNull(imageId) ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)
        return entity.toDomain()
    }

    override fun updateUrlInfo(image: Image): Image {
        val entity = imageRepository.findByIdOrNull(image.imageId) ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)
        entity.modifyUrlInfo(image.url, image.parId)
        return entity.toDomain()
    }

    override fun delete(imageId: Long) {
        imageRepository.deleteById(imageId)
    }
}
