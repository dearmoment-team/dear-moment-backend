package kr.kro.dearmoment.image.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.image.domain.Image

@RepositoryTest
class ImagePersistenceAdapterTest(
    private val imageRepository: JpaImageRepository,
) : BehaviorSpec({
        val adapter = ImagePersistenceAdapter(imageRepository)

        Given("이미지를 파라미터로 제공했을 때") {
            val image =
                Image(
                    userId = 123L,
                    url = "localhost:8080/image",
                    fileName = "image.jpg",
                )

            When("DB 저장을 하게 되면") {
                Then("이미지의 ID를 반환한다.") {
                    val result = adapter.save(image)
                    result.shouldNotBeNull()
                }
            }
        }

        Given("이미지 ID를 파라미터로 제공했을 때") {
            val image =
                Image(
                    userId = 123L,
                    url = "localhost:8080/image",
                    fileName = "image.jpg",
                )

            val expectedId = adapter.save(image)

            When("DB에 해당 이미지에 대한 조회 시") {
                Then("이미지가 존재하면 이미지를 반환한다.") {
                    val result = adapter.findOne(expectedId)
                    result.shouldNotBeNull()
                    result.imageId shouldBe expectedId
                }

                Then("이미지가 존재하지 않으면 예외를 발생 시킨다.") {
                    shouldThrow<IllegalArgumentException> { adapter.findOne(9999999L) }
                }
            }

            When("이미지를 삭제하면") {
                Then("DB에 이미지를 삭제한다.") {
                    shouldNotThrow<Throwable> { adapter.delete(expectedId) }
                }
            }
        }

        Given("유저 ID를 파라미터로 제공했을 때") {
            val userId = 123L
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

            When("이미지를 조회하면") {
                Then("유저의 모든 이미지를 조회 및 반환한다.") {
                    images.forEach { adapter.save(it) }
                    val result = adapter.findAll(123L)
                    result.size shouldBe images.size
                }
            }
        }
    })
