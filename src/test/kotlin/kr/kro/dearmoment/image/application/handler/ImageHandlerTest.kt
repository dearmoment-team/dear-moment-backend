package kr.kro.dearmoment.image.application.handler

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.AdditionalImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.SubImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateAdditionalImageAction
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import org.springframework.web.multipart.MultipartFile

class ImageHandlerTest : StringSpec({

    // 목 인스턴스 생성 (각 테스트 전에 초기화)
    lateinit var imageService: ImageService
    lateinit var imageHandler: ImageHandler

    beforeTest {
        imageService = mockk(relaxed = true)
        imageHandler = ImageHandler(imageService)
    }

    "updateMainImage - 새 파일 업로드 후 기존 이미지 삭제" {
        // Given
        val userId = 1L
        val currentImage =
            Image(
                imageId = 100L,
                userId = userId,
                fileName = "old_main.jpg",
                url = "http://test.com/old_main.jpg",
                parId = "",
            )
        val newFile: MultipartFile = mockk()
        val uploadedImage =
            Image(
                imageId = 200L,
                userId = userId,
                fileName = "new_main.jpg",
                url = "http://test.com/new_main.jpg",
                parId = "",
            )
        every { imageService.save(match { it.file == newFile && it.userId == userId }) } returns uploadedImage
        justRun { imageService.delete(currentImage.imageId) }

        // When
        val result = imageHandler.updateMainImage(newFile, userId, currentImage)

        // Then
        result shouldBe uploadedImage
        verify(exactly = 1) { imageService.save(match { it.file == newFile && it.userId == userId }) }
        verify(exactly = 1) { imageService.delete(currentImage.imageId) }
    }

    "processSubImagesFinal - 요청된 서브 이미지 수가 4장이 아니면 예외" {
        val userId = 1L
        val currentSubImages = emptyList<Image>()
        // UPLOAD 액션에 해당하는 파일 리스트는 여기서는 빈 리스트 전달 (finalRequests의 개수와 비교됨)
        val finalRequests = listOf<SubImageFinalRequest>() // size 0

        shouldThrow<IllegalArgumentException> {
            imageHandler.processSubImagesFinal(currentSubImages, finalRequests, emptyList(), userId)
        }.message shouldBe "서브 이미지는 정확히 4장이어야 합니다. 현재 0장입니다."
    }

    "processSubImagesFinal - ALL KEEP" {
        // Given
        val userId = 1L
        val currentSubImages =
            listOf(
                Image(11L, userId, "sub1.jpg", "url1", ""),
                Image(12L, userId, "sub2.jpg", "url2", ""),
                Image(13L, userId, "sub3.jpg", "url3", ""),
                Image(14L, userId, "sub4.jpg", "url4", ""),
            )
        val finalRequests =
            currentSubImages.map {
                SubImageFinalRequest(UpdateSubImageAction.KEEP, imageId = it.imageId)
            }
        // UPLOAD 액션이 없으므로 업로드 파일 리스트는 빈 리스트 전달
        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, emptyList(), userId)
        // Then
        result shouldHaveSize 4
        result shouldBe currentSubImages
    }

    "processSubImagesFinal - ALL DELETE" {
        // Given
        val userId = 1L
        val currentSubImages =
            listOf(
                Image(11L, userId, "sub1.jpg", "url1", ""),
                Image(12L, userId, "sub2.jpg", "url2", ""),
                Image(13L, userId, "sub3.jpg", "url3", ""),
                Image(14L, userId, "sub4.jpg", "url4", ""),
            )
        val finalRequests =
            currentSubImages.map {
                SubImageFinalRequest(UpdateSubImageAction.DELETE, imageId = it.imageId)
            }
        // 각 이미지 삭제에 대해 목 호출 처리
        currentSubImages.forEach { img ->
            justRun { imageService.delete(img.imageId) }
        }
        // UPLOAD 파일 리스트는 빈 리스트 전달
        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, emptyList(), userId)
        // Then
        result shouldHaveSize 0
        currentSubImages.forEach { img ->
            verify(exactly = 1) { imageService.delete(img.imageId) }
        }
    }

    "processSubImagesFinal - KEEP, DELETE, UPLOAD 액션 정상 수행" {
        // Given
        val userId = 1L
        val existingImg1 = Image(imageId = 11L, userId = userId, fileName = "sub1.jpg", url = "url1", parId = "")
        val existingImg2 = Image(imageId = 22L, userId = userId, fileName = "sub2.jpg", url = "url2", parId = "")
        val existingImg3 = Image(imageId = 33L, userId = userId, fileName = "sub3.jpg", url = "url3", parId = "")
        val existingImg4 = Image(imageId = 44L, userId = userId, fileName = "sub4.jpg", url = "url4", parId = "")
        val currentSubImages = listOf(existingImg1, existingImg2, existingImg3, existingImg4)
        // 요청 4개: 1) KEEP(11) 2) DELETE(22) 3) UPLOAD (기존 33 교체) 4) UPLOAD (새로 업로드)
        val newFile3 = mockk<MultipartFile>()
        val newFile4 = mockk<MultipartFile>()
        val uploadedImage3 = Image(imageId = 55L, userId = userId, fileName = "sub5.jpg", url = "url5", parId = "")
        val uploadedImage4 = Image(imageId = 66L, userId = userId, fileName = "sub6.jpg", url = "url6", parId = "")
        every { imageService.save(match { it.file == newFile3 && it.userId == userId }) } returns uploadedImage3
        every { imageService.save(match { it.file == newFile4 && it.userId == userId }) } returns uploadedImage4
        justRun { imageService.delete(existingImg2.imageId) } // DELETE 액션
        justRun { imageService.delete(existingImg3.imageId) } // UPLOAD 교체 시 기존 이미지 삭제
        val finalRequests =
            listOf(
                SubImageFinalRequest(UpdateSubImageAction.KEEP, imageId = 11L),
                SubImageFinalRequest(UpdateSubImageAction.DELETE, imageId = 22L),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, imageId = 33L),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, imageId = null),
            )
        // 업로드 파일 리스트는 순서대로 newFile3, newFile4
        val subImageFiles = listOf(newFile3, newFile4)
        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, subImageFiles, userId)
        // Then
        result.size shouldBe 3
        result[0] shouldBe existingImg1
        result[1] shouldBe uploadedImage3
        result[2] shouldBe uploadedImage4
        verify(exactly = 1) { imageService.delete(existingImg2.imageId) }
        verify(exactly = 1) { imageService.delete(existingImg3.imageId) }
        verify(exactly = 1) { imageService.save(match { it.file == newFile3 && it.userId == userId }) }
        verify(exactly = 1) { imageService.save(match { it.file == newFile4 && it.userId == userId }) }
    }

    "processAdditionalImagesFinal - 요청된 추가 이미지 수가 최대 개수(5장)를 초과하면 예외" {
        val userId = 1L
        val currentAdditionalImages = emptyList<Image>()
        // 6개의 UPLOAD 요청
        val finalRequests =
            List(6) {
                AdditionalImageFinalRequest(
                    action = UpdateAdditionalImageAction.UPLOAD,
                    imageId = null,
                )
            }
        shouldThrow<IllegalArgumentException> {
            imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, null, userId)
        }.message shouldBe "추가 이미지는 최대 5 장까지만 가능합니다. 현재 6장입니다."
    }

    "processAdditionalImagesFinal - ALL KEEP" {
        val userId = 1L
        val existingAdd1 = Image(1001L, userId, "add1.jpg", "addUrl1", "")
        val existingAdd2 = Image(1002L, userId, "add2.jpg", "addUrl2", "")
        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)
        val finalRequests =
            currentAdditionalImages.map {
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.KEEP, imageId = it.imageId)
            }
        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, null, userId)
        result shouldHaveSize 2
        result shouldBe currentAdditionalImages
    }

    "processAdditionalImagesFinal - ALL DELETE" {
        val userId = 1L
        val existingAdd1 = Image(1001L, userId, "add1.jpg", "addUrl1", "")
        val existingAdd2 = Image(1002L, userId, "add2.jpg", "addUrl2", "")
        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)
        val finalRequests =
            currentAdditionalImages.map {
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, imageId = it.imageId)
            }
        justRun { imageService.delete(existingAdd1.imageId) }
        justRun { imageService.delete(existingAdd2.imageId) }
        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, null, userId)
        result shouldHaveSize 0
        verify(exactly = 1) { imageService.delete(existingAdd1.imageId) }
        verify(exactly = 1) { imageService.delete(existingAdd2.imageId) }
    }

    "processAdditionalImagesFinal - KEEP, DELETE, UPLOAD 액션 정상 수행" {
        // Given
        val userId = 1L
        val existingAdd1 = Image(imageId = 1001L, userId = userId, "add1.jpg", "addUrl1", "")
        val existingAdd2 = Image(imageId = 1002L, userId = userId, "add2.jpg", "addUrl2", "")
        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)
        // 요청 3개: 1) KEEP(1001), 2) DELETE(1002), 3) UPLOAD(새)
        val newFile = mockk<MultipartFile>()
        val uploadedAdd =
            Image(
                imageId = 2002L,
                userId = userId,
                fileName = "add_new.jpg",
                url = "http://test.com/add_new.jpg",
                parId = "",
            )
        every { imageService.save(match { it.file == newFile && it.userId == userId }) } returns uploadedAdd
        justRun { imageService.delete(existingAdd2.imageId) }
        val finalRequests =
            listOf(
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.KEEP, imageId = 1001L),
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, imageId = 1002L),
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.UPLOAD, imageId = null),
            )
        val additionalImageFiles = listOf(newFile)
        // When
        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, additionalImageFiles, userId)
        // Then
        result.size shouldBe 2
        result[0] shouldBe existingAdd1
        result[1] shouldBe uploadedAdd
        verify(exactly = 1) { imageService.delete(existingAdd2.imageId) }
        verify(exactly = 1) { imageService.save(match { it.file == newFile && it.userId == userId }) }
    }
})
