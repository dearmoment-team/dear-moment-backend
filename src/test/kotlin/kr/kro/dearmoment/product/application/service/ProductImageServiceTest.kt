package kr.kro.dearmoment.product.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse
import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class ProductImageServiceTest : BehaviorSpec({
    // imageService는 모킹하고, productImageService는 내부 메소드 호출 모킹을 위해 spyk로 생성합니다.
    val imageService = mockk<ImageService>(relaxed = true)
    val productImageService = spyk(ProductImageServiceImpl(imageService))
    val userId = 1L

    // --- uploadImages 메소드 테스트 ---
    given("uploadImages 메소드") {
        `when`("유효한 MultipartFile 리스트가 주어지면") {
            val file = MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".toByteArray())
            val multipartFiles: List<MultipartFile> = listOf(file)
            every {
                imageService.saveAll(
                    match { commands ->
                        commands.size == 1 && commands[0].file.originalFilename == "test.jpg"
                    },
                )
            } returns listOf(100L)
            every { imageService.getOne(100L) } returns
                GetImageResponse(
                    imageId = 100L,
                    url = "http://example.com/test.jpg",
                )

            val images = productImageService.uploadImages(multipartFiles, userId)
            then("올바른 Image 객체 리스트를 반환해야 한다") {
                images shouldBe
                    listOf(
                        Image(imageId = 100L, userId = userId, fileName = "test.jpg", url = "http://example.com/test.jpg"),
                    )
            }
            verify {
                imageService.saveAll(match { it.size == 1 })
                imageService.getOne(100L)
            }
        }
    }

    // --- uploadNewImagesWithPlaceholders 메소드 테스트 ---
    given("uploadNewImagesWithPlaceholders 메소드") {
        `when`("newImages가 null 또는 empty이면") {
            then("빈 맵을 반환한다") {
                productImageService.uploadNewImagesWithPlaceholders(null, userId) shouldBe emptyMap()
                productImageService.uploadNewImagesWithPlaceholders(emptyList(), userId) shouldBe emptyMap()
            }
        }
        `when`("newImages가 존재하면") {
            val file = MockMultipartFile("file", "new.jpg", "image/jpeg", "new image".toByteArray())
            val newImages: List<MultipartFile> = listOf(file)
            // uploadImages 메소드 내부 호출을 모킹합니다.
            val uploadedImage = Image(imageId = 101L, userId = userId, fileName = "new.jpg", url = "http://example.com/new.jpg")
            every { productImageService.uploadImages(newImages, userId) } returns listOf(uploadedImage)

            val mapping = productImageService.uploadNewImagesWithPlaceholders(newImages, userId)
            then("매핑 키와 Image 객체가 올바르게 반환되어야 한다") {
                mapping shouldBe mapOf("new_0" to uploadedImage)
            }
        }
    }

    given("resolveFinalImageOrder 메소드") {
        `when`("identifier가 URL인 경우") {
            val identifier = "http://example.com/abc.jpg"
            val result = productImageService.resolveFinalImageOrder(listOf(identifier), emptyMap(), userId)
            then("URL에서 파일명을 추출하여 Image 객체를 생성한다") {
                result.size shouldBe 1
                result[0].fileName shouldBe "abc.jpg"
                result[0].url shouldBe identifier
            }
        }
        `when`("identifier가 placeholder인 경우") {
            val placeholder = "new_0"
            val newImage = Image(imageId = 102L, userId = userId, fileName = "placeholder.jpg", url = "http://example.com/placeholder.jpg")
            val mapping = mapOf(placeholder to newImage)
            val result = productImageService.resolveFinalImageOrder(listOf(placeholder), mapping, userId)
            then("매핑된 Image 객체를 반환한다") {
                result.size shouldBe 1
                result[0] shouldBe newImage
            }
        }
    }

    given("synchronizeProductImages 메소드") {
        `when`("기존 ProductEntity의 이미지와 최종 Image 리스트가 주어지면") {
            val productEntity =
                ProductEntity().apply {
                    images.addAll(
                        listOf(
                            ImageEntity(
                                id = 1L,
                                userId = userId,
                                fileName = "old.jpg",
                                url = "http://example.com/old.jpg",
                            ),
                            ImageEntity(
                                id = 2L,
                                userId = userId,
                                fileName = "toDelete.jpg",
                                url = "http://example.com/toDelete.jpg",
                            ),
                        ),
                    )
                }
            // 최종 이미지 리스트: "old.jpg"와 신규 이미지 "new.jpg"
            val finalImages =
                listOf(
                    Image(imageId = 1L, userId = userId, fileName = "old.jpg", url = "http://example.com/old.jpg"),
                    Image(imageId = 0L, userId = userId, fileName = "new.jpg", url = "http://example.com/new.jpg"),
                )
            // 신규 이미지 매핑 (createNewImageEntity 메소드에서 참조됨)
            val newImageMapping = mapOf("new_0" to finalImages[1])

            // 삭제되어야 할 기존 이미지(파일명이 "toDelete.jpg")의 삭제 메소드를 모킹합니다.
            every { imageService.delete(2L) } just Runs

            productImageService.synchronizeProductImages(productEntity, finalImages, newImageMapping, userId)
            then("삭제되어야 하는 이미지는 delete가 호출되고, 최종 이미지 리스트로 업데이트된다") {
                productEntity.images.map { it.fileName } shouldContainExactly listOf("old.jpg", "new.jpg")
                verify(exactly = 1) { imageService.delete(2L) }
            }
        }
    }
})
