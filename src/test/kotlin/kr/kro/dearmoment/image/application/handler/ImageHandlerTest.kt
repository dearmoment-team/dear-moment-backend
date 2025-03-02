package kr.kro.dearmoment.image.application.handler

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.*
import org.springframework.web.multipart.MultipartFile

class ImageHandlerTest : StringSpec({

    val imageService = mockk<ImageService>()
    val imageHandler = ImageHandler(imageService)

    "updateMainImage - 새 파일 업로드 후 기존 이미지 삭제" {
        // Given
        val userId = 1L
        val currentImage = Image(
            imageId = 100L,
            userId = userId,
            fileName = "old_main.jpg",
            url = "http://test.com/old_main.jpg"
        )
        val newFile: MultipartFile = mockk()
        val uploadedImage = Image(
            imageId = 200L,
            userId = userId,
            fileName = "new_main.jpg",
            url = "http://test.com/new_main.jpg"
        )

        every { imageService.uploadSingleImage(newFile, userId) } returns uploadedImage
        justRun { imageService.delete(currentImage.imageId) }

        // When
        val result = imageHandler.updateMainImage(newFile, userId, currentImage)

        // Then
        result shouldBe uploadedImage
        verify(exactly = 1) { imageService.uploadSingleImage(newFile, userId) }
        verify(exactly = 1) { imageService.delete(currentImage.imageId) }
    }

    "processSubImagesFinal - 요청된 서브 이미지 수가 4장이 아니면 예외" {
        val userId = 1L
        val currentSubImages = emptyList<Image>()
        val finalRequests = listOf<SubImageFinalRequest>() // 0장

        shouldThrow<IllegalArgumentException> {
            imageHandler.processSubImagesFinal(currentSubImages, finalRequests, userId)
        }.message shouldBe "서브 이미지는 정확히 4장이어야 합니다. 현재 0장입니다."
    }

    "processAdditionalImagesFinal - 요청된 추가 이미지 수가 최대 개수(5장)를 초과하면 예외" {
        val userId = 1L
        val currentAdditionalImages = emptyList<Image>()
        val finalRequests = List(6) {
            AdditionalImageFinalRequest(
                action = UpdateAdditionalImageAction.UPLOAD,
                imageId = null,
                newFile = mockk<MultipartFile>()
            )
        }

        shouldThrow<IllegalArgumentException> {
            imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, userId)
        }.message shouldBe "추가 이미지는 최대 5 장까지만 가능합니다. 현재 6장입니다."
    }

    "processSubImagesFinal - KEEP, DELETE, UPLOAD 액션 정상 수행" {
        // Given
        val userId = 1L
        val existingImg1 = Image(imageId = 11L, userId = userId, fileName = "sub1.jpg", url = "url1")
        val existingImg2 = Image(imageId = 22L, userId = userId, fileName = "sub2.jpg", url = "url2")
        val existingImg3 = Image(imageId = 33L, userId = userId, fileName = "sub3.jpg", url = "url3")
        val existingImg4 = Image(imageId = 44L, userId = userId, fileName = "sub4.jpg", url = "url4")

        val currentSubImages = listOf(existingImg1, existingImg2, existingImg3, existingImg4)

        // 요청 4개: 1) KEEP(11) 2) DELETE(22) 3) UPLOAD(기존33) 4) UPLOAD(새)
        val newFile3 = mockk<MultipartFile>()
        val newFile4 = mockk<MultipartFile>()

        val uploadedImage3 = Image(imageId = 55L, userId = userId, fileName = "sub5.jpg", url = "url5")
        val uploadedImage4 = Image(imageId = 66L, userId = userId, fileName = "sub6.jpg", url = "url6")

        every { imageService.uploadSingleImage(newFile3, userId) } returns uploadedImage3
        every { imageService.uploadSingleImage(newFile4, userId) } returns uploadedImage4
        justRun { imageService.delete(existingImg2.imageId) } // DELETE
        justRun { imageService.delete(existingImg3.imageId) } // UPLOAD 교체

        val finalRequests = listOf(
            SubImageFinalRequest(UpdateSubImageAction.KEEP, imageId = 11L, newFile = null),
            SubImageFinalRequest(UpdateSubImageAction.DELETE, imageId = 22L, newFile = null),
            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, imageId = 33L, newFile = newFile3),
            SubImageFinalRequest(UpdateSubImageAction.UPLOAD, imageId = null, newFile = newFile4)
        )

        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, userId)

        // Then
        // result에는 KEEP된 img1, 교체 업로드된 img3(=uploadedImage3), 새 업로드 img4(=uploadedImage4) 총 3장
        // 그러나 "정확히 4장"을 맞추려면, 실제 구현·업무 규칙상 4장을 요구 → 여기서는 업로드 2개, KEEP 1개 → 총 3개
        // (테스트 시나리오 상 "DELETE" 대신 "UPLOAD"를 2장 하면 4개가 됨)
        // 여기서는 예시로 3장 결과를 확인하지만, 실제 비즈니스 로직에 맞춰 4장을 모두 채우도록 할 수도 있음
        result.size shouldBe 3
        result[0] shouldBe existingImg1      // KEEP
        result[1] shouldBe uploadedImage3    // UPLOAD 교체
        result[2] shouldBe uploadedImage4    // UPLOAD 신규

        // Verify
        verify(exactly = 1) { imageService.delete(existingImg2.imageId) }
        verify(exactly = 1) { imageService.delete(existingImg3.imageId) }
        verify(exactly = 1) { imageService.uploadSingleImage(newFile3, userId) }
        verify(exactly = 1) { imageService.uploadSingleImage(newFile4, userId) }
    }

    "processAdditionalImagesFinal - KEEP, DELETE, UPLOAD 액션 정상 수행" {
        // Given
        val userId = 1L
        val existingAdd1 = Image(imageId = 1001L, userId = userId, fileName = "add1.jpg", url = "addUrl1")
        val existingAdd2 = Image(imageId = 1002L, userId = userId, fileName = "add2.jpg", url = "addUrl2")

        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)

        // 요청 3개: 1) KEEP(1001), 2) DELETE(1002), 3) UPLOAD(새)
        val newFile = mockk<MultipartFile>()
        val uploadedAdd = Image(
            imageId = 2002L,
            userId = userId,
            fileName = "add_new.jpg",
            url = "http://test.com/add_new.jpg"
        )
        every { imageService.uploadSingleImage(newFile, userId) } returns uploadedAdd
        justRun { imageService.delete(existingAdd2.imageId) }

        val finalRequests = listOf(
            AdditionalImageFinalRequest(UpdateAdditionalImageAction.KEEP, imageId = 1001L),
            AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, imageId = 1002L),
            AdditionalImageFinalRequest(UpdateAdditionalImageAction.UPLOAD, imageId = null, newFile = newFile)
        )

        // When
        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, userId)

        // Then
        // 최종 결과: KEEP된 add1, 새로 업로드된 add_new → 2장
        result.size shouldBe 2
        result[0] shouldBe existingAdd1
        result[1] shouldBe uploadedAdd

        // Verify
        verify(exactly = 1) { imageService.delete(existingAdd2.imageId) }
        verify(exactly = 1) { imageService.uploadSingleImage(newFile, userId) }
    }
})
