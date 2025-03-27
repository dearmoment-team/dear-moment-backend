package kr.kro.dearmoment.image.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.port.input.UpdateImagePort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromDBPort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImagePort
import kr.kro.dearmoment.image.application.port.output.SaveImagePort
import kr.kro.dearmoment.image.application.port.output.UploadImagePort
import kr.kro.dearmoment.image.domain.Image
import java.time.LocalDateTime
import java.util.UUID

class ImageServiceTest : BehaviorSpec({

    val uploadImagePort = mockk<UploadImagePort>()
    val saveImagePort = mockk<SaveImagePort>()
    val getImagePort = mockk<GetImagePort>()
    val updateImagePort = mockk<UpdateImagePort>()
    val deleteImageFromDbPort = mockk<DeleteImageFromDBPort>()
    val deleteImageFromObjectStoragePort = mockk<DeleteImageFromObjectStoragePort>()
    val getImageFromObjectStoragePort = mockk<GetImageFromObjectStoragePort>()
    val imageService =
        ImageService(
            uploadImagePort,
            saveImagePort,
            getImagePort,
            updateImagePort,
            deleteImageFromDbPort,
            getImageFromObjectStoragePort,
            deleteImageFromObjectStoragePort,
        )

    // dummy userId (UUID)
    val dummyUserId: UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    Given("파일과 유저 ID를 포함한 저장 명령을 제공했을 때") {
        val file = mockk<org.springframework.web.multipart.MultipartFile>()
        val saveImageCommand = SaveImageCommand(file, dummyUserId)
        val uploadedImage =
            Image(
                imageId = 1L,
                userId = dummyUserId,
                url = "localhost:8080/image",
                fileName = "image.jpg",
            )
        val expectedImage =
            Image(
                imageId = 456L,
                userId = dummyUserId,
                url = "localhost:8080/image",
                fileName = "image.jpg",
            )

        every { uploadImagePort.upload(file, dummyUserId) } returns uploadedImage
        every { saveImagePort.save(uploadedImage) } returns expectedImage

        When("이미지를 저장하면") {
            val result = imageService.save(saveImageCommand)

            Then("이미지를 저장하고 이미지 객체를 반환한다.") {
                result shouldBe expectedImage
                verify(exactly = 1) { uploadImagePort.upload(file, dummyUserId) }
                verify(exactly = 1) { saveImagePort.save(uploadedImage) }
            }
        }
    }

    Given("이미지 ID를 제공했을 때") {
        val image =
            Image(
                userId = dummyUserId,
                imageId = 1L,
                url = "localhost:8080/image",
                parId = "parId",
                fileName = "image.jpg",
            )

        val updatedImage =
            Image(
                userId = dummyUserId,
                imageId = 1L,
                url = "localhost:8080/image/change",
                parId = "changedParId",
                fileName = "image.jpg",
            )

        When("이미지를 조회하면") {
            every { getImagePort.findOne(1L) } returns image
            every { getImageFromObjectStoragePort.getImageWithUrl(image) } returns updatedImage
            every { updateImagePort.updateUrlInfo(updatedImage) } returns updatedImage

            val result = imageService.getOne(1L)

            Then("이미지 정보에 대한 응답을 반환한다.") {
                result.imageId shouldBe image.imageId
                result.url shouldBe updatedImage.url
                verify(exactly = 1) { getImagePort.findOne(1L) }
            }
        }
    }

    Given("delete()는") {
        val image =
            Image(
                userId = dummyUserId,
                imageId = 1L,
                url = "localhost:8080/image",
                parId = "parId",
                fileName = "image.jpg",
            )
        When("이미지를 삭제하면") {
            clearMocks(getImagePort, deleteImageFromDbPort, deleteImageFromObjectStoragePort)

            every { getImagePort.findOne(1L) } returns image
            every { deleteImageFromDbPort.delete(1L) } just Runs
            every { deleteImageFromObjectStoragePort.delete(image) } just Runs

            Then("해당 이미지를 객체 스토리지와 DB에서 삭제한다.") {
                shouldNotThrow<Throwable> { imageService.delete(1L) }
                verify(exactly = 1) { getImagePort.findOne(1L) }
                verify(exactly = 1) { deleteImageFromObjectStoragePort.delete(image) }
                verify(exactly = 1) { deleteImageFromDbPort.delete(1L) }
            }
        }
    }

    Given("delete()는 원자성을 보장하기 위해") {
        val image =
            Image(
                userId = dummyUserId,
                imageId = 1L,
                url = "localhost:8080/image",
                parId = "parId",
                fileName = "image.jpg",
            )
        When("이미지 삭제시 객체 스토리지 삭제에 실패하면") {
            clearMocks(getImagePort, deleteImageFromDbPort, deleteImageFromObjectStoragePort)

            every { getImagePort.findOne(1L) } returns image
            every { deleteImageFromObjectStoragePort.delete(image) } throws CustomException(ErrorCode.IMAGE_DELETE_FAIL_FROM_OBJECT_STORAGE)

            Then("예외를 발생시키고 DB 삭제가 실행되지 않는다.") {
                shouldThrow<CustomException> { imageService.delete(1L) }

                verify(exactly = 1) { getImagePort.findOne(1L) }
                verify(exactly = 1) { deleteImageFromObjectStoragePort.delete(image) }
                verify(exactly = 0) { deleteImageFromDbPort.delete(1L) }
            }
        }
    }

    Given("유저 ID를 파라미터로 제공했을 때") {
        val images =
            listOf(
                Image(
                    userId = dummyUserId,
                    imageId = 1L,
                    url = "localhost:8080/image",
                    fileName = "image.jpg",
                    urlExpireTime = LocalDateTime.now().plusDays(1L),
                ),
                Image(
                    userId = dummyUserId,
                    imageId = 2L,
                    url = "localhost:8080/image",
                    fileName = "image22.jpg",
                    urlExpireTime = LocalDateTime.now().plusDays(1L),
                ),
            )
        every { getImagePort.findUserImages(dummyUserId) } returns images
        every { getImageFromObjectStoragePort.getImageWithUrl(any()) } returns
            Image(
                userId = dummyUserId,
                fileName = "",
                imageId = 1L,
                url = "",
                parId = "",
            )

        When("이미지를 조회하면") {
            val result = imageService.getAll(dummyUserId)
            Then("해당 유저의 모든 이미지 정보에 대한 응답을 반환한다.") {
                result.images.size shouldBe images.size
                verify(exactly = 1) { getImagePort.findUserImages(dummyUserId) }
            }
        }
    }
})
