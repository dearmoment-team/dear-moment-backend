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

    // --- 서브 이미지 부분 업데이트 (processSubImagesPartial) 테스트 ---
    "processSubImagesPartial - 인덱스 범위 벗어나면 예외" {
        val userId = 1L
        // 서브 이미지는 반드시 4장이어야 함
        val currentSubImages =
            listOf(
                Image(11L, userId, "sub1.jpg", "url1", ""),
                Image(12L, userId, "sub2.jpg", "url2", ""),
                Image(13L, userId, "sub3.jpg", "url3", ""),
                Image(14L, userId, "sub4.jpg", "url4", ""),
            )
        // finalRequests에서 인덱스 4는 유효 범위(0~3)를 벗어남
        val finalRequests =
            listOf(
                SubImageFinalRequest(UpdateSubImageAction.KEEP, index = 0, imageId = 11L),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, index = 4, imageId = null),
            )
        val subImageFiles = listOf(mockk<MultipartFile>())
        shouldThrow<IllegalArgumentException> {
            imageHandler.processSubImagesPartial(currentSubImages, finalRequests, subImageFiles, userId)
        }.message shouldBe "서브 이미지는 인덱스 0..3 범위만 허용됩니다. (index=4)"
    }

    "processSubImagesPartial - ALL KEEP" {
        val userId = 1L
        val currentSubImages =
            listOf(
                Image(11L, userId, "sub1.jpg", "url1", ""),
                Image(12L, userId, "sub2.jpg", "url2", ""),
                Image(13L, userId, "sub3.jpg", "url3", ""),
                Image(14L, userId, "sub4.jpg", "url4", ""),
            )
        val finalRequests =
            currentSubImages.mapIndexed { index, img ->
                SubImageFinalRequest(UpdateSubImageAction.KEEP, index = index, imageId = img.imageId)
            }
        val result = imageHandler.processSubImagesPartial(currentSubImages, finalRequests, emptyList(), userId)
        result shouldHaveSize 4
        result shouldBe currentSubImages
    }

    "processSubImagesPartial - DELETE 액션 단독 호출 시 예외 발생" {
        val userId = 1L
        val currentSubImages =
            listOf(
                Image(11L, userId, "sub1.jpg", "url1", ""),
                Image(12L, userId, "sub2.jpg", "url2", ""),
                Image(13L, userId, "sub3.jpg", "url3", ""),
                Image(14L, userId, "sub4.jpg", "url4", ""),
            )
        // 인덱스 0에 대해 DELETE 단독 호출 (UPLOAD와 함께 사용하지 않음)
        val finalRequests =
            listOf(
                SubImageFinalRequest(UpdateSubImageAction.DELETE, index = 0, imageId = 11L),
            )
        shouldThrow<IllegalArgumentException> {
            imageHandler.processSubImagesPartial(currentSubImages, finalRequests, emptyList(), userId)
        }.message shouldBe "index=0 에서 DELETE 액션은 UPLOAD 액션과 함께 사용해야 합니다."
    }

    "processSubImagesPartial - KEEP와 UPLOAD 액션 정상 수행" {
        val userId = 1L
        val existingImg1 = Image(11L, userId, "sub1.jpg", "url1", "")
        val existingImg2 = Image(22L, userId, "sub2.jpg", "url2", "")
        val existingImg3 = Image(33L, userId, "sub3.jpg", "url3", "")
        val existingImg4 = Image(44L, userId, "sub4.jpg", "url4", "")
        val currentSubImages = listOf(existingImg1, existingImg2, existingImg3, existingImg4)

        // 요청:
        // index 0: KEEP
        // index 1: KEEP
        // index 2: DELETE + UPLOAD (기존 이미지 교체)
        // index 3: UPLOAD (새 이미지 업로드)
        val newFileForIdx2 = mockk<MultipartFile>()
        val newFileForIdx3 = mockk<MultipartFile>()
        val uploadedImageForIdx2 = Image(55L, userId, "sub_new.jpg", "url_new", "")
        val uploadedImageForIdx3 = Image(66L, userId, "sub_new2.jpg", "url_new2", "")

        every { imageService.save(match { it.file == newFileForIdx2 && it.userId == userId }) } returns uploadedImageForIdx2
        every { imageService.save(match { it.file == newFileForIdx3 && it.userId == userId }) } returns uploadedImageForIdx3
        justRun { imageService.delete(existingImg3.imageId) }

        val finalRequests =
            listOf(
                SubImageFinalRequest(UpdateSubImageAction.KEEP, index = 0, imageId = existingImg1.imageId),
                SubImageFinalRequest(UpdateSubImageAction.KEEP, index = 1, imageId = existingImg2.imageId),
                SubImageFinalRequest(UpdateSubImageAction.DELETE, index = 2, imageId = existingImg3.imageId),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, index = 2, imageId = existingImg3.imageId),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, index = 3, imageId = null),
            )
        val subImageFiles = listOf(newFileForIdx2, newFileForIdx3)

        val result = imageHandler.processSubImagesPartial(currentSubImages, finalRequests, subImageFiles, userId)

        result.size shouldBe 4
        result[0] shouldBe existingImg1
        result[1] shouldBe existingImg2
        result[2] shouldBe uploadedImageForIdx2
        result[3] shouldBe uploadedImageForIdx3

        verify(exactly = 1) { imageService.delete(existingImg3.imageId) }
        verify(exactly = 1) { imageService.save(match { it.file == newFileForIdx2 && it.userId == userId }) }
        verify(exactly = 1) { imageService.save(match { it.file == newFileForIdx3 && it.userId == userId }) }
    }

    // --- 추가 이미지 최종 처리 (processAdditionalImagesFinal) 테스트 ---
    "processAdditionalImagesFinal - 추가 이미지 결과 개수가 최대 개수(5장)를 초과하면 예외" {
        val userId = 1L
        val currentAdditionalImages = emptyList<Image>()
        // 6개의 UPLOAD 요청 생성
        val finalRequests =
            List(6) {
                AdditionalImageFinalRequest(
                    action = UpdateAdditionalImageAction.UPLOAD,
                    imageId = null,
                )
            }
        // UPLOAD 요청 수와 동일하게 6개의 파일 제공
        val additionalImageFiles = List(6) { mockk<MultipartFile>() }
        shouldThrow<IllegalArgumentException> {
            imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, additionalImageFiles, userId)
        }.message shouldBe "추가 이미지는 최대 5 장까지만 가능합니다. (현재=6장)"
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
        val userId = 1L
        val existingAdd1 = Image(imageId = 1001L, userId = userId, fileName = "add1.jpg", url = "addUrl1", parId = "")
        val existingAdd2 = Image(imageId = 1002L, userId = userId, fileName = "add2.jpg", url = "addUrl2", parId = "")
        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)
        // 요청: 1) KEEP(1001), 2) DELETE(1002), 3) UPLOAD(새 이미지)
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
        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, additionalImageFiles, userId)
        result.size shouldBe 2
        result[0] shouldBe existingAdd1
        result[1] shouldBe uploadedAdd
        verify(exactly = 1) { imageService.delete(existingAdd2.imageId) }
        verify(exactly = 1) { imageService.save(match { it.file == newFile && it.userId == userId }) }
    }
})
