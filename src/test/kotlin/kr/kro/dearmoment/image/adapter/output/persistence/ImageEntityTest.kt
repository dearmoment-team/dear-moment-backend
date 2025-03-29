package kr.kro.dearmoment.image.adapter.output.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class ImageEntityTest : DescribeSpec({

    describe("ImageEntity") {
        val userId = UUID.randomUUID()
        val initialUrl = "https://example.com/image.jpg"
        val initialParId = "12345"
        val fileName = "image.jpg"

        context("생성") {
            it("정상적으로 엔티티를 생성해야 한다") {
                val imageEntity =
                    ImageEntity(
                        userId = userId,
                        fileName = fileName,
                        url = initialUrl,
                        parId = initialParId,
                    )

                imageEntity.userId shouldBe userId
                imageEntity.url shouldBe initialUrl
                imageEntity.fileName shouldBe fileName
                imageEntity.parId shouldBe initialParId
            }
        }

        context("URL 정보 수정") {
            it("URL과 parId를 수정할 수 있어야 한다") {
                val imageEntity =
                    ImageEntity(
                        userId = userId,
                        fileName = fileName,
                        url = initialUrl,
                        parId = initialParId,
                    )
                val modifiedUrl = "https://example.com/updated.jpg"
                val modifiedParId = "67890"

                imageEntity.modifyUrlInfo(modifiedUrl, modifiedParId)

                imageEntity.url shouldBe modifiedUrl
                imageEntity.parId shouldBe modifiedParId
            }
        }

        context("도메인 변환") {
            it("Image 도메인 객체로 변환할 수 있어야 한다") {
                val imageEntity =
                    ImageEntity(
                        userId = userId,
                        fileName = fileName,
                        url = initialUrl,
                        parId = initialParId,
                    )
                val domain = imageEntity.toDomain()

                domain.imageId shouldBe imageEntity.id
                domain.userId shouldBe imageEntity.userId
                domain.url shouldBe imageEntity.url
                domain.fileName shouldBe imageEntity.fileName
                domain.parId shouldBe imageEntity.parId
            }
        }
    }
})
