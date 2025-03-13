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
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import org.springframework.web.multipart.MultipartFile

class ImageHandlerTest : StringSpec({

    // 각 테스트마다 새로운 목 인스턴스를 생성하도록 beforeTest 블록에서 초기화합니다.
    lateinit var imageService: ImageService
    lateinit var imageHandler: ImageHandler

    beforeTest {
        imageService = mockk(relaxed = true)
        imageHandler = ImageHandler(imageService)
    }

    "mergeUpdateRequest - subImagesFinal / subImageFiles 개수 불일치 시 예외" {
        // Given
        val productId = 999L
        val rawRequest =
            UpdateProductRequest(
                productId = 0L,
                userId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Dummy",
                subImagesFinal =
                    listOf(
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockk()),
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, mockk()),
                    ),
            )
        // 실제로는 1개만 넘어옴
        val subImageFiles = listOf(mockk<MultipartFile>())

        // When + Then
        val ex =
            shouldThrow<IllegalArgumentException> {
                imageHandler.mergeUpdateRequest(
                    productId = productId,
                    rawRequest = rawRequest,
                    mainImageFile = null,
                    subImageFiles = subImageFiles,
                    additionalImageFiles = null,
                )
            }
        ex.message shouldBe "서브 이미지(UPLOAD) 항목 개수(2)와 업로드된 파일 개수(1)가 다릅니다."
    }

    "mergeUpdateRequest - 정상 매핑 시 productId와 mainImageFile이 덮어씌워진다" {
        // Given
        val productId = 999L
        val newMainFile: MultipartFile = mockk()
        val rawRequest =
            UpdateProductRequest(
                productId = 0L,
                userId = 10L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Before Merge",
                mainImageFile = null,
                subImagesFinal =
                    listOf(
                        SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, null),
                        SubImageFinalRequest(UpdateSubImageAction.KEEP, 123L, null),
                    ),
            )
        // 실제 서브 이미지 파일은 1개
        val subImageFiles = listOf(mockk<MultipartFile>())

        // When
        val merged =
            imageHandler.mergeUpdateRequest(
                productId = productId,
                rawRequest = rawRequest,
                mainImageFile = newMainFile,
                subImageFiles = subImageFiles,
                additionalImageFiles = null,
            )

        // Then
        merged.productId shouldBe 999L
        merged.mainImageFile shouldBe newMainFile
        merged.subImagesFinal[0].newFile shouldBe subImageFiles[0]
        merged.subImagesFinal[1].newFile shouldBe null
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
            )
        val newFile: MultipartFile = mockk()
        val uploadedImage =
            Image(
                imageId = 200L,
                userId = userId,
                fileName = "new_main.jpg",
                url = "http://test.com/new_main.jpg",
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
        val finalRequests = listOf<SubImageFinalRequest>()

        shouldThrow<IllegalArgumentException> {
            imageHandler.processSubImagesFinal(currentSubImages, finalRequests, userId)
        }.message shouldBe "서브 이미지는 정확히 4장이어야 합니다. 현재 0장입니다."
    }

    "processSubImagesFinal - ALL KEEP" {
        // Given
        val userId = 1L
        val currentSubImages =
            listOf(
                Image(11L, userId, fileName = "sub1.jpg", url = "url1"),
                Image(12L, userId, fileName = "sub2.jpg", url = "url2"),
                Image(13L, userId, fileName = "sub3.jpg", url = "url3"),
                Image(14L, userId, fileName = "sub4.jpg", url = "url4"),
            )
        val finalRequests =
            currentSubImages.map {
                SubImageFinalRequest(UpdateSubImageAction.KEEP, imageId = it.imageId, newFile = null)
            }

        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, userId)

        // Then
        result shouldHaveSize 4
        result shouldBe currentSubImages
    }

    "processSubImagesFinal - ALL DELETE" {
        // Given
        val userId = 1L
        val currentSubImages =
            listOf(
                Image(11L, userId, fileName = "sub1.jpg", url = "url1"),
                Image(12L, userId, fileName = "sub2.jpg", url = "url2"),
                Image(13L, userId, fileName = "sub3.jpg", url = "url3"),
                Image(14L, userId, fileName = "sub4.jpg", url = "url4"),
            )

        val finalRequests =
            currentSubImages.map {
                SubImageFinalRequest(UpdateSubImageAction.DELETE, imageId = it.imageId, newFile = null)
            }

        // 각 이미지 삭제에 대해 목 호출 처리
        currentSubImages.forEach { img ->
            justRun { imageService.delete(img.imageId) }
        }

        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, userId)

        // Then
        result shouldHaveSize 0
        currentSubImages.forEach { img ->
            verify(exactly = 1) { imageService.delete(img.imageId) }
        }
    }

    "processSubImagesFinal - KEEP, DELETE, UPLOAD 액션 정상 수행" {
        // Given
        val userId = 1L
        val existingImg1 = Image(imageId = 11L, userId = userId, fileName = "sub1.jpg", url = "url1")
        val existingImg2 = Image(imageId = 22L, userId = userId, fileName = "sub2.jpg", url = "url2")
        val existingImg3 = Image(imageId = 33L, userId = userId, fileName = "sub3.jpg", url = "url3")
        val existingImg4 = Image(imageId = 44L, userId = userId, fileName = "sub4.jpg", url = "url4")

        val currentSubImages = listOf(existingImg1, existingImg2, existingImg3, existingImg4)

        // 요청 4개: 1) KEEP(11) 2) DELETE(22) 3) UPLOAD(기존33 교체) 4) UPLOAD(새)
        val newFile3 = mockk<MultipartFile>()
        val newFile4 = mockk<MultipartFile>()

        val uploadedImage3 = Image(imageId = 55L, userId = userId, fileName = "sub5.jpg", url = "url5")
        val uploadedImage4 = Image(imageId = 66L, userId = userId, fileName = "sub6.jpg", url = "url6")

        every { imageService.save(match { it.file == newFile3 && it.userId == userId }) } returns uploadedImage3
        every { imageService.save(match { it.file == newFile4 && it.userId == userId }) } returns uploadedImage4
        justRun { imageService.delete(existingImg2.imageId) } // DELETE 액션
        justRun { imageService.delete(existingImg3.imageId) } // UPLOAD 교체 시 기존 이미지 삭제

        val finalRequests =
            listOf(
                SubImageFinalRequest(UpdateSubImageAction.KEEP, imageId = 11L, newFile = null),
                SubImageFinalRequest(UpdateSubImageAction.DELETE, imageId = 22L, newFile = null),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, imageId = 33L, newFile = newFile3),
                SubImageFinalRequest(UpdateSubImageAction.UPLOAD, imageId = null, newFile = newFile4),
            )

        // When
        val result = imageHandler.processSubImagesFinal(currentSubImages, finalRequests, userId)

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
                    newFile = mockk<MultipartFile>(),
                )
            }

        shouldThrow<IllegalArgumentException> {
            imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, userId)
        }.message shouldBe "추가 이미지는 최대 5 장까지만 가능합니다. 현재 6장입니다."
    }

    "processAdditionalImagesFinal - ALL KEEP" {
        val userId = 1L
        val existingAdd1 = Image(1001L, userId, fileName = "add1.jpg", url = "addUrl1")
        val existingAdd2 = Image(1002L, userId, fileName = "add2.jpg", url = "addUrl2")

        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)

        // 모두 KEEP
        val finalRequests =
            currentAdditionalImages.map {
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.KEEP, it.imageId, null)
            }

        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, userId)
        result shouldHaveSize 2
        result shouldBe currentAdditionalImages
    }

    "processAdditionalImagesFinal - ALL DELETE" {
        val userId = 1L
        val existingAdd1 = Image(1001L, userId, fileName = "add1.jpg", url = "addUrl1")
        val existingAdd2 = Image(1002L, userId, fileName = "add2.jpg", url = "addUrl2")

        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)

        // 모두 DELETE
        val finalRequests =
            currentAdditionalImages.map {
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, it.imageId, null)
            }

        justRun { imageService.delete(existingAdd1.imageId) }
        justRun { imageService.delete(existingAdd2.imageId) }

        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, userId)
        result shouldHaveSize 0
        verify(exactly = 1) { imageService.delete(existingAdd1.imageId) }
        verify(exactly = 1) { imageService.delete(existingAdd2.imageId) }
    }

    "processAdditionalImagesFinal - KEEP, DELETE, UPLOAD 액션 정상 수행" {
        // Given
        val userId = 1L
        val existingAdd1 = Image(imageId = 1001L, userId = userId, fileName = "add1.jpg", url = "addUrl1")
        val existingAdd2 = Image(imageId = 1002L, userId = userId, fileName = "add2.jpg", url = "addUrl2")

        val currentAdditionalImages = listOf(existingAdd1, existingAdd2)

        // 요청 3개: 1) KEEP(1001), 2) DELETE(1002), 3) UPLOAD(새)
        val newFile = mockk<MultipartFile>()
        val uploadedAdd =
            Image(
                imageId = 2002L,
                userId = userId,
                fileName = "add_new.jpg",
                url = "http://test.com/add_new.jpg",
            )

        every { imageService.save(match { it.file == newFile && it.userId == userId }) } returns uploadedAdd
        justRun { imageService.delete(existingAdd2.imageId) }

        val finalRequests =
            listOf(
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.KEEP, imageId = 1001L),
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, imageId = 1002L),
                AdditionalImageFinalRequest(UpdateAdditionalImageAction.UPLOAD, imageId = null, newFile = newFile),
            )

        // When
        val result = imageHandler.processAdditionalImagesFinal(currentAdditionalImages, finalRequests, userId)

        // Then
        result.size shouldBe 2
        result[0] shouldBe existingAdd1
        result[1] shouldBe uploadedAdd

        verify(exactly = 1) { imageService.delete(existingAdd2.imageId) }
        verify(exactly = 1) { imageService.save(match { it.file == newFile && it.userId == userId }) }
    }
})
