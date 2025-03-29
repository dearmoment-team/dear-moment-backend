package kr.kro.dearmoment.image.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.image.domain.Image
import java.util.UUID

@RepositoryTest
class ImagePersistenceAdapterTest(
    private val imageRepository: JpaImageRepository,
) : BehaviorSpec({

        // Adapter 인스턴스 생성
        val adapter = ImagePersistenceAdapter(imageRepository)

        // dummy userId (UUID)
        val dummyUserId: UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        Given("이미지를 파라미터로 제공했을 때") {
            val image =
                Image(
                    userId = dummyUserId,
                    url = "localhost:8080/image",
                    fileName = "image.jpg",
                )

            When("DB 저장을 하게 되면") {
                Then("저장된 이미지 객체를 반환한다.") {
                    val savedImage = adapter.save(image)
                    savedImage.shouldNotBeNull()
                    savedImage.imageId.shouldNotBeNull()
                }
            }
        }

        Given("이미지가 저장된 상태에서 변경할 이미지를 파라미터로 제공했을 때") {
            val image =
                Image(
                    userId = dummyUserId,
                    url = "localhost:8080/image",
                    parId = "parId",
                    fileName = "image.jpg",
                )
            val savedImage = adapter.save(image)
            When("이후 DB에 해당 이미지 url을 업데이트 하면") {
                val renewedImage =
                    Image(
                        imageId = savedImage.imageId,
                        userId = dummyUserId,
                        url = "localhost:8080/image/update",
                        parId = "updatedParId",
                        fileName = "image.jpg",
                    )
                Then("변경된 이미지 객체를 반환한다.") {
                    val updated = adapter.updateUrlInfo(renewedImage)
                    updated.parId shouldBe renewedImage.parId
                    updated.url shouldBe renewedImage.url
                }
            }
        }

        Given("복수개의 이미지 파라미터로 제공했을 때") {
            val userId = dummyUserId
            val images =
                listOf(
                    Image(
                        userId = userId,
                        url = "localhost:8080/image",
                        fileName = "image.jpg",
                    ),
                    Image(
                        userId = userId,
                        url = "localhost:8080/image",
                        fileName = "image22.jpg",
                    ),
                )

            When("DB 저장을 하게 되면") {
                Then("저장된 이미지 객체 리스트를 반환한다.") {
                    val savedImages = adapter.saveAll(images)
                    savedImages.shouldNotBeNull()
                    savedImages.size shouldBe images.size
                }
            }
        }

        Given("이미지 ID를 파라미터로 제공했을 때") {
            val image =
                Image(
                    userId = dummyUserId,
                    url = "localhost:8080/image",
                    fileName = "image.jpg",
                )
            val savedImage = adapter.save(image)

            When("DB에 해당 이미지에 대한 조회 시") {
                Then("이미지가 존재하면 이미지를 반환한다.") {
                    val found = adapter.findOne(savedImage.imageId)
                    found.shouldNotBeNull()
                    found.imageId shouldBe savedImage.imageId
                }

                Then("이미지가 존재하지 않으면 예외를 발생시킨다.") {
                    shouldThrow<CustomException> { adapter.findOne(9999999L) }
                }
            }

            When("이미지를 삭제하면") {
                Then("DB에서 해당 이미지를 삭제한다.") {
                    shouldNotThrow<Throwable> { adapter.delete(savedImage.imageId) }
                }
            }
        }

        Given("유저 ID를 파라미터로 제공했을 때") {
            val userId = dummyUserId
            val images =
                listOf(
                    Image(
                        userId = userId,
                        url = "localhost:8080/image",
                        fileName = "image.jpg",
                    ),
                    Image(
                        userId = userId,
                        url = "localhost:8080/image",
                        fileName = "image22.jpg",
                    ),
                )
            images.forEach { adapter.save(it) }

            When("이미지를 조회하면") {
                Then("해당 유저의 모든 이미지를 조회 및 반환한다.") {
                    val result = adapter.findUserImages(userId)
                    result.size shouldBe images.size
                }
            }
        }
    })
