package kr.kro.dearmoment.image.application.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.port.input.UpdateImagePort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromDBPort
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImagePort
import kr.kro.dearmoment.image.application.port.output.SaveImagePort
import kr.kro.dearmoment.image.application.port.output.UploadImagePort
import kr.kro.dearmoment.image.domain.Image
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

class ImageServiceTest : FunSpec({

    // 모의 객체(Mockk)로 의존성 선언
    val uploadImagePort = mockk<UploadImagePort>()
    val saveImagePort = mockk<SaveImagePort>()
    val getImagePort = mockk<GetImagePort>()
    val updateImagePort = mockk<UpdateImagePort>()
    val deleteImageFromDBPort = mockk<DeleteImageFromDBPort>()
    val getImageFromObjectStorage = mockk<GetImageFromObjectStoragePort>()
    val deleteImageFromObjectStorage = mockk<DeleteImageFromObjectStoragePort>()

    // 테스트 대상 서비스 생성
    val imageService = ImageService(
        uploadImagePort,
        saveImagePort,
        getImagePort,
        updateImagePort,
        deleteImageFromDBPort,
        getImageFromObjectStorage,
        deleteImageFromObjectStorage
    )

    beforeTest {
        // 각 테스트 시작 전 모든 모의 객체 초기화
        clearAllMocks()
    }

    test("save 메서드는 이미지를 업로드한 후 DB에 저장하고, DTO 변환 결과를 검증한다") {
        // Given
        val file = mockk<MultipartFile>()
        val userId = 1L
        val command = SaveImageCommand(file, userId)
        val uploadedImage = Image(
            imageId = 100L,
            userId = userId,
            parId = "dummyParId",
            fileName = "dummyFileName",
            url = "dummyUrl",
            // 미래 시간으로 고정 (예: 2025-03-17 12:00)
            urlExpireTime = LocalDateTime.of(2025, 3, 17, 12, 0)
        )
        val savedImage = uploadedImage.copy(imageId = 101L)

        every { uploadImagePort.upload(file, userId) } returns uploadedImage
        every { saveImagePort.save(uploadedImage) } returns savedImage

        // When
        val result = imageService.save(command)

        // Then
        result shouldBe savedImage
        // DTO 변환 검증
        val dto = GetImageResponse.from(result)
        dto.imageId shouldBe savedImage.imageId
        dto.url shouldBe savedImage.url

        verify { uploadImagePort.upload(file, userId) }
        verify { saveImagePort.save(uploadedImage) }
    }

    test("getOne 메서드는 URL이 만료된 경우 갱신된 URL 정보를 포함하는 DTO를 반환해야 한다") {
        // Given
        val imageId = 200L
        // 만료된 URL: 2025-03-15 12:00 (현재보다 이전)
        val expiredImage = Image(
            imageId = imageId,
            userId = 2L,
            parId = "expiredParId",
            fileName = "expiredFileName",
            url = "oldUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 15, 12, 0)
        )
        // 갱신된 이미지: 미래 시간 (2025-03-17 12:00)
        val renewedImage = expiredImage.copy(
            parId = "newParId",
            url = "newUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 17, 12, 0)
        )

        every { getImagePort.findOne(imageId) } returns expiredImage
        every { getImageFromObjectStorage.getImageWithUrl(expiredImage) } returns renewedImage
        every { updateImagePort.updateUrlInfo(renewedImage) } returns renewedImage

        // When
        val response = imageService.getOne(imageId)

        // Then
        response.imageId shouldBe renewedImage.imageId
        response.url shouldBe renewedImage.url

        verify { getImagePort.findOne(imageId) }
        verify { getImageFromObjectStorage.getImageWithUrl(expiredImage) }
        verify { updateImagePort.updateUrlInfo(renewedImage) }
    }

    test("getOne 메서드는 URL이 유효한 경우 그대로 DTO로 반환해야 한다") {
        // Given
        val imageId = 300L
        // 유효한 URL: 2025-03-17 12:00 (현재보다 확실히 미래)
        val validImage = Image(
            imageId = imageId,
            userId = 3L,
            parId = "validParId",
            fileName = "validFileName",
            url = "validUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 17, 12, 0)
        )

        every { getImagePort.findOne(imageId) } returns validImage

        // When
        val response = imageService.getOne(imageId)

        // Then
        response.imageId shouldBe validImage.imageId
        response.url shouldBe validImage.url

        verify { getImagePort.findOne(imageId) }
        // 갱신 로직이 호출되지 않아야 함
        verify(exactly = 0) { getImageFromObjectStorage.getImageWithUrl(any()) }
        verify(exactly = 0) { updateImagePort.updateUrlInfo(any()) }
    }

    test("getAll 메서드는 만료된 이미지의 경우 갱신 후, 모든 이미지를 DTO 목록으로 반환해야 한다") {
        // Given
        val userId = 4L
        // 만료된 이미지: 2025-03-15 12:00
        val expiredImage = Image(
            imageId = 400L,
            userId = userId,
            parId = "expiredParId",
            fileName = "expiredFileName",
            url = "oldUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 15, 12, 0)
        )
        // 유효한 이미지: 2025-03-17 12:00
        val validImage = Image(
            imageId = 401L,
            userId = userId,
            parId = "validParId",
            fileName = "validFileName",
            url = "validUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 17, 12, 0)
        )
        val renewedImage = expiredImage.copy(
            parId = "newParId",
            url = "newUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 17, 12, 0)
        )

        every { getImagePort.findUserImages(userId) } returns listOf(expiredImage, validImage)
        every { getImageFromObjectStorage.getImageWithUrl(expiredImage) } returns renewedImage

        // When
        val response = imageService.getAll(userId)

        // Then
        response.images.size shouldBe 2
        // 갱신된 이미지의 DTO 검증
        val expiredDto = response.images.find { it.imageId == expiredImage.imageId }
        expiredDto?.url shouldBe renewedImage.url
        // 유효한 이미지의 DTO 검증
        val validDto = response.images.find { it.imageId == validImage.imageId }
        validDto?.url shouldBe validImage.url

        verify { getImagePort.findUserImages(userId) }
        verify { getImageFromObjectStorage.getImageWithUrl(expiredImage) }
    }

    test("delete 메서드는 이미지 스토리지와 DB에서 이미지를 삭제해야 한다") {
        // Given
        val imageId = 500L
        val image = Image(
            imageId = imageId,
            userId = 5L,
            parId = "dummyParId",
            fileName = "dummyFileName",
            url = "dummyUrl",
            urlExpireTime = LocalDateTime.of(2025, 3, 17, 12, 0)
        )

        every { getImagePort.findOne(imageId) } returns image
        every { deleteImageFromObjectStorage.delete(image) } just Runs
        every { deleteImageFromDBPort.delete(imageId) } just Runs

        // When
        imageService.delete(imageId)

        // Then
        verify { getImagePort.findOne(imageId) }
        verify { deleteImageFromObjectStorage.delete(image) }
        verify { deleteImageFromDBPort.delete(imageId) }
    }
})
