package kr.kro.dearmoment.image.application.service

import kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse
import kr.kro.dearmoment.image.adapter.input.web.dto.GetImagesResponse
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.port.input.DeleteImageUseCase
import kr.kro.dearmoment.image.application.port.input.GetImageUseCase
import kr.kro.dearmoment.image.application.port.input.SaveImageUseCase
import kr.kro.dearmoment.image.application.port.input.UpdateImagePort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromDBPort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImagePort
import kr.kro.dearmoment.image.application.port.output.SaveImagePort
import kr.kro.dearmoment.image.application.port.output.UploadImagePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImageService(
    private val uploadImagePort: UploadImagePort,
    private val saveImagePort: SaveImagePort,
    private val getImagePort: GetImagePort,
    private val updateImagePort: UpdateImagePort,
    private val deleteImageFromDBPort: DeleteImageFromDBPort,
    private val getImageFromObjectStorage: GetImageFromObjectStoragePort,
    private val deleteImageFromObjectStorage: DeleteImageFromObjectStoragePort,
) : SaveImageUseCase, DeleteImageUseCase, GetImageUseCase {
    @Transactional
    override fun save(saveImageCommand: SaveImageCommand): Long {
        val image = uploadImagePort.upload(saveImageCommand.file, saveImageCommand.userId)
        return saveImagePort.save(image)
    }

    @Transactional
    override fun saveAll(commands: List<SaveImageCommand>): List<Long> {
        val images = uploadImagePort.uploadAll(commands)
        return saveImagePort.saveAll(images)
    }

    @Transactional(readOnly = true)
    override fun getOne(imageId: Long): GetImageResponse {
        val image = getImagePort.findOne(imageId)

        if (image.isUrlExpired()) {
            val renewedImage = getImageFromObjectStorage.getImageWithUrl(image)

            check(updateImagePort.update(renewedImage) > 0) { "fail update" }

            return GetImageResponse.from(renewedImage)
        }

        return GetImageResponse.from(image)
    }

    @Transactional(readOnly = true)
    override fun getAll(userId: Long): GetImagesResponse {
        val images = getImagePort.findAll(userId)

        val finalResult =
            images.map { image ->
                if (image.isUrlExpired()) {
                    getImageFromObjectStorage.getImageWithUrl(image)
                } else {
                    image
                }
            }

        return GetImagesResponse.from(finalResult)
    }

    @Transactional
    override fun delete(imageId: Long) {
        val image = getImagePort.findOne(imageId)

        deleteImageFromObjectStorage.delete(image)
        deleteImageFromDBPort.delete(imageId)
    }
}
