package kr.kro.dearmoment.product.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity

class ProductImageServiceTest : BehaviorSpec({

    val imageService = mockk<ImageService>(relaxed = true)
    // ProductImageServiceImpl은 upload 관련 메소드를 제거하고, resolveFinalImageOrder와 synchronizeProductImages만 제공
    val productImageService = spyk(ProductImageServiceImpl(imageService))
    val userId = 1L

    given("resolveFinalImageOrder 메소드") {
        `when`("identifier가 URL인 경우") {
            val identifier = "http://example.com/abc.jpg"
            val result = productImageService.resolveFinalImageOrder(listOf(identifier), emptyMap(), userId)
            then("URL에서 파일명을 추출하여 Image 객체를 생성한다") {
                result.size shouldBe 1
                result[0].fileName shouldBe "abc.jpg"
                result[0].url shouldBe identifier
                // urlExpireTime은 현재 시각 기준으로 생성되므로 null이 아님을 검증
                result[0].urlExpireTime shouldNotBe null
            }
        }

        `when`("identifier가 placeholder인 경우") {
            val placeholder = "new_0"
            val newImage =
                Image(
                    imageId = 102L,
                    userId = userId,
                    fileName = "placeholder.jpg",
                    url = "http://example.com/placeholder.jpg",
                )
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
                            ImageEntity(id = 1L, userId = userId, fileName = "old.jpg", url = "http://example.com/old.jpg"),
                            ImageEntity(id = 2L, userId = userId, fileName = "toDelete.jpg", url = "http://example.com/toDelete.jpg"),
                        ),
                    )
                }
            // 최종 이미지 리스트에는 "old.jpg"와 신규 이미지 "new.jpg"가 포함된다.
            val finalImages =
                listOf(
                    Image(imageId = 1L, userId = userId, fileName = "old.jpg", url = "http://example.com/old.jpg"),
                    Image(imageId = 0L, userId = userId, fileName = "new.jpg", url = "http://example.com/new.jpg"),
                )
            // 신규 이미지 매핑 (예: placeholder "new_0" → 해당 Image)
            val newImageMapping = mapOf("new_0" to finalImages[1])
            // "toDelete.jpg"가 최종 목록에 없으므로 삭제되어야 함을 모킹
            every { imageService.delete(2L) } just Runs

            productImageService.synchronizeProductImages(productEntity, finalImages, newImageMapping, userId)

            then("삭제되어야 하는 이미지는 delete가 호출되고, 최종 이미지 리스트로 업데이트된다") {
                productEntity.images.map { it.fileName } shouldContainExactly listOf("old.jpg", "new.jpg")
                verify(exactly = 1) { imageService.delete(2L) }
            }
        }
    }
})
